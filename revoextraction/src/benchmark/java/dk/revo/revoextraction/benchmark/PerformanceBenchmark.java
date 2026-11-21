package dk.revo.revoextraction.benchmark;

import com.google.gson.GsonBuilder;
import dk.revo.revoextraction.*;
import dk.revo.revocultivation.*;
import dk.revo.revostorage.*;
import dk.revo.revoindustry.hatchery.*;
import dk.revo.revoindustry.saltern.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Opt-in benchmark only: excluded from distributable jars. Never opens a player save. */
@EventBusSubscriber(modid="revoextraction",value=Dist.CLIENT)
public final class PerformanceBenchmark {
 private static final String WORLD="Revo Performance Benchmark";
 private static final Path OUT=Path.of(System.getProperty("revo.benchmark.output"));
 private static final int WARMUP=600,SAMPLE=1200;
 private static final List<Long> tickTimes=new ArrayList<>(),frameTimes=new ArrayList<>(),warmFrameTimes=new ArrayList<>();
 private static final Map<String,Object> result=new LinkedHashMap<>();
 private static boolean launched,form,creating,preparing,done,reloading,inspecting,reloadDone;
 private static volatile boolean ready,measuring,finished;
 private static volatile int ticks;
 private static long preTick,lastFrame,lastWarmFrame,measureStart,measureEnd,titleTime,reloadStart;
 private static int titleFrames,inspectionFrames,reloadFrames;
 private static StorageMenu storageMenu;

 private static long uptime(){return ManagementFactory.getRuntimeMXBean().getUptime();}
 private static Block block(String id){var b=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));if(b==Blocks.AIR)throw new IllegalStateException("Missing benchmark block "+id);return b;}
 private static void put(ServerLevel l,BlockPos p,Block b){l.setBlockAndUpdate(p,b.defaultBlockState());}
 private static void power(ServerLevel l,BlockPos consumer){put(l,consumer.below(),block("modern_industrialization:creative_storage_unit"));}
 private static void shot(String name){try{Files.createDirectories(OUT);try(var image=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}}catch(Exception e){throw new RuntimeException(e);}}
 private static void write(String name){try{Files.createDirectories(OUT);Files.writeString(OUT.resolve(name),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");}catch(Exception e){throw new RuntimeException(e);}}

 private static FarmMachine farm(ServerLevel l,MachineKind kind,BlockPos origin,int species){
  l.setBlockAndUpdate(origin,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.NORTH));var m=(FarmMachine)l.getBlockEntity(origin);
  for(int x=-kind.radius();x<=kind.radius();x++)for(int y=0;y<kind.height;y++)for(int z=-kind.radius();z<=kind.radius();z++){
   var part=kind.part(x,y,z);var s=kind.block(part).defaultBlockState();
   if(part==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,Direction.NORTH);
   if(part==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,Direction.EAST);
   if(part==MachineKind.Part.FILTER)s=s.setValue(FarmFilter.FACING,Direction.NORTH).setValue(FarmFilter.SPECIES,species);
   if(part==MachineKind.Part.POWER)s=s.setValue(BlockStateProperties.FACING,Direction.SOUTH);
   l.setBlockAndUpdate(m.at(x,y,z),s);
  }
  if(!m.checkStructure())throw new IllegalStateException("Benchmark farm failed: "+kind);
  if(kind.needsWater())m.water.fill(new FluidStack(Fluids.WATER,16000),FluidAction.EXECUTE);
  if(!kind.isHarvester())m.inputs.setStackInSlot(0,MachineKind.starter(kind.isTree()?0:10));
  if(kind.electric())power(l,m.at(kind.radius(),0,kind.hatchZ()));
  return m;
 }

 private static void prepare(ServerPlayer player){
  var l=player.serverLevel();var path=l.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
  if(!path.toString().contains("performance-benchmark")||!path.toString().contains(WORLD))throw new IllegalStateException("Refusing non-benchmark world: "+path);
  l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.setRainLevel(0);
  l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());
  player.setGameMode(GameType.CREATIVE);player.getInventory().clearContent();player.getAbilities().flying=true;player.onUpdateAbilities();
  for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(-60,158,-20,60,178,76),e->!(e instanceof net.minecraft.world.entity.player.Player)))e.discard();
  for(int x=-56;x<=56;x++)for(int z=-16;z<=72;z++){
   l.setBlock(new BlockPos(x,159,z),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),2);
   for(int y=160;y<=174;y++)l.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
  }
  // Removing filled machines legitimately drops their contents; clear those reset-only entities too.
  for(var entity:l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(-60,158,-20,60,178,76),entity->!(entity instanceof net.minecraft.world.entity.player.Player)))entity.discard();
  for(int i=0;i<32;i++){var pos=new BlockPos(-24+(i%8)*4,160,48+(i/8)*4);put(l,pos,RevoExtraction.ELECTRIC.get());var e=(ElectricDrillEntity)l.getBlockEntity(pos);e.program=i%6==5?7:i%6;power(l,pos);}
  for(int i=0;i<12;i++)farm(l,MachineKind.STEEL_HARVESTER,new BlockPos(-24+(i%6)*8,160,8+(i/6)*8),i%8);
  for(int i=0;i<4;i++)farm(l,MachineKind.GREENHOUSE,new BlockPos(-24+i*8,160,26),0);
  for(int i=0;i<4;i++)farm(l,MachineKind.IRON_GREENHOUSE,new BlockPos(8+i*8,160,26),0);
  for(int i=0;i<4;i++)farm(l,MachineKind.STEEL_TREE,new BlockPos(-24+i*8,160,38),0);
  for(int i=0;i<8;i++){
   var pos=new BlockPos(-16+i,160,-6);put(l,pos,RevoStorage.DRIVE.get());var d=(StorageEntity)l.getBlockEntity(pos);
   for(int b=0;b<9;b++)d.chests.setStackInSlot(b,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("ironchest:diamond_chest"))));
   for(int s=0;s<108;s++)d.banks[0].setStackInSlot(s,new ItemStack(new Item[]{Items.IRON_INGOT,Items.COPPER_INGOT,Items.REDSTONE,Items.DIAMOND,Items.PAPER,Items.OAK_LOG}[s%6],32));
  }
  var terminalPos=new BlockPos(-8,160,-6);put(l,terminalPos,RevoStorage.TERMINAL.get());var terminal=(StorageEntity)l.getBlockEntity(terminalPos);put(l,terminalPos.east(),RevoStorage.SPRING.get());var spring=l.getBlockEntity(terminalPos.east());var springData=spring.saveWithoutMetadata(l.registryAccess());springData.putInt("Charge",256);spring.loadWithComponents(springData,l.registryAccess());storageMenu=new StorageMenu(901,player.getInventory(),terminal);
  for(int row=0;row<8;row++){for(int col=0;col<16;col++){
   var p=new BlockPos(-48+col,160,row);l.setBlockAndUpdate(p,Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.EAST));((HopperBlockEntity)l.getBlockEntity(p)).setItem(0,new ItemStack(Items.COBBLESTONE,16));
  }put(l,new BlockPos(-32,160,row),Blocks.CHEST);}
  for(int x=36;x<=52;x++)for(int z=38;z<=54;z++)if(x==36||x==52||z==38||z==54)put(l,new BlockPos(x,160,z),Blocks.OAK_FENCE);
  for(int i=0;i<64;i++){var cow=EntityType.COW.create(l);cow.getRandom().setSeed(151126L+i);cow.moveTo(37.5+i%8*1.8,160,39.5+i/8*1.8,0,0);cow.setPersistenceRequired();l.addFreshEntity(cow);}
  for(int i=0;i<2;i++){
   var p=new BlockPos(32,160,8+i*8);for(var cell:SalternLayout.cells())l.setBlockAndUpdate(SalternLayout.at(p,Direction.NORTH,cell.offset()),SalternLayout.state(cell,Direction.NORTH));
   var c=(SalternController)l.getBlockEntity(p);if(!c.validate())throw new IllegalStateException(c.problem());c.water.fill(new FluidStack(Fluids.WATER,8000),FluidAction.EXECUTE);
   put(l,c.at(0,0,3),block("modern_industrialization:creative_storage_unit"));var centrifuge=c.at(2,0,1);put(l,centrifuge,block("modern_industrialization:centrifuge"));power(l,centrifuge);
  }
  var hatcheryPos=new BlockPos(24,160,6);for(var cell:HatcheryLayout.SHELL)l.setBlockAndUpdate(HatcheryLayout.at(hatcheryPos,Direction.NORTH,cell.offset()),HatcheryLayout.state(cell,Direction.NORTH));
  var hatchery=(HatcheryController)l.getBlockEntity(hatcheryPos);if(!hatchery.validate())throw new IllegalStateException(hatchery.problem());hatchery.input().items(null).insertItem(0,new ItemStack(Hatchery.EGG.get(),4),false);put(l,hatchery.at(0,0,5),block("modern_industrialization:creative_storage_unit"));
  player.teleportTo(0,178,82);l.setDefaultSpawnPos(new BlockPos(0,160,-10),0);
  try{Files.writeString(path.resolve("BENCHMARK-README.txt"),"Revo Performance Benchmark: creative test save. 32 electric drills, 12 steel harvesters, 8 greenhouses, 4 real-tree growers, 8 nine-chest drives (7776 slots), 128 hoppers, 64 cows, 2 salt chambers + native centrifuges and a real Wither hatchery. Creative MI sources power the machines. Water/input buffers are finite; the benchmark tool resets the scene before each measured run. Not a survival/progression save.\n");}catch(Exception e){throw new RuntimeException(e);}
  long looseItems=l.getEntitiesOfClass(ItemEntity.class,new AABB(-60,158,-20,60,178,76)).size();if(looseItems!=0)throw new IllegalStateException("Reset left loose items: "+looseItems);result.put("resetLooseItems",looseItems);
  result.put("fixture",Map.of("electricDrills",32,"harvesters",12,"greenhouses",8,"treeGrowers",4,"storageSlots",7776,"hoppers",128,"cows",64,"saltChambers",2,"witherHatcheries",1));ready=true;
 }

 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void pre(ServerTickEvent.Pre e){if(ready)preTick=System.nanoTime();}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void post(ServerTickEvent.Post e){
  if(!ready)return;if(storageMenu!=null)storageMenu.broadcastChanges();long end=System.nanoTime();
  if(ticks==WARMUP){measuring=true;measureStart=end;}
  if(ticks>=WARMUP&&ticks<WARMUP+SAMPLE)tickTimes.add(end-preTick);
  if(++ticks>=WARMUP+SAMPLE){
   measureEnd=end;measuring=false;ready=false;var level=e.getServer().overworld();
   var farm=(FarmMachine)level.getBlockEntity(new BlockPos(-24,160,8));var salt=(SalternController)level.getBlockEntity(new BlockPos(32,160,8));var hatchery=(HatcheryController)level.getBlockEntity(new BlockPos(24,160,6));
   int stars=0;var out=hatchery.output().items(null);for(int i=0;i<out.getSlots();i++)if(out.getStackInSlot(i).is(Items.NETHER_STAR))stars+=out.getStackInSlot(i).getCount();
   result.put("workProof",Map.of("harvesterCycles",farm.data(11),"saltCycles",salt.cycles(),"netherStars",stars,"looseItemsAtEnd",level.getEntitiesOfClass(ItemEntity.class,new AABB(-60,158,-20,60,178,76),entity->entity.isAlive()).size()));
   if(farm.data(11)==0||salt.cycles()==0||stars==0){result.put("error","Fixture production did not run");write("failure.json");}finished=true;
  }
 }
 @SubscribeEvent public static void frame(RenderFrameEvent.Post e){long now=System.nanoTime();if(measuring){if(lastFrame!=0)frameTimes.add(now-lastFrame);lastFrame=now;}else {lastFrame=0;if(ready){if(lastWarmFrame!=0)warmFrameTimes.add(now-lastWarmFrame);lastWarmFrame=now;}else lastWarmFrame=0;}}
 private static Map<String,Double> stats(List<Long> values){if(values.isEmpty())throw new IllegalStateException("No benchmark samples");var sorted=values.stream().mapToDouble(v->v/1e6).sorted().toArray();return Map.of("mean",Arrays.stream(sorted).average().orElseThrow(),"median",sorted[sorted.length/2],"p95",sorted[(int)((sorted.length-1)*.95)],"max",sorted[sorted.length-1]);}
 private static void models(){
  var mc=Minecraft.getInstance();var missing=mc.getModelManager().getMissingModel();var failures=new ArrayList<String>();
  var names=new ArrayList<String>(List.of("revoindustry:block/geode_crystals","revoindustry:block/geode_water_volume","revoindustry:block/wither_egg_cracked_2","revoindustry:block/wither_roof_lamp_lit"));
  for(var kind:MachineKind.values())if(kind.registered())names.add("revocultivation:block/"+kind.id+"_body");
  for(String name:names)if(mc.getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.parse(name)))==missing)failures.add(name);
  if(!failures.isEmpty())throw new IllegalStateException("Missing benchmark models "+failures);result.put("modelChecks",names.size());
 }

 @SubscribeEvent public static void client(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.getOverlay()!=null)return;
  try{
   if(Files.exists(OUT.resolve("stop-request"))){done=true;mc.stop();return;}
   if(!launched&&mc.screen instanceof TitleScreen){
    if(titleTime==0){titleTime=uptime();result.put("titleReadyMs",titleTime);result.put("java",System.getProperty("java.version"));var pluginClass=Class.forName("org.embeddedt.modernfix.core.ModernFixMixinPlugin");var plugin=pluginClass.getField("instance").get(null);result.put("dynamicResourcesEffective",pluginClass.getMethod("getOptionValue",String.class,Class.class).invoke(plugin,"mixin.perf.dynamic_resources",Boolean.class));try(var code=PerformanceBenchmark.class.getResourceAsStream("PerformanceBenchmark.class")){result.put("probeSha256",java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(code.readAllBytes())));}result.put("maxHeapBytes",Runtime.getRuntime().maxMemory());result.put("lithium",ModList.get().isLoaded("lithium"));result.put("loadedModCount",ModList.get().size());write("startup.json");shot("title");}
    if(++titleFrames<60)return;
    launched=true;if(Boolean.getBoolean("revo.benchmark.titleOnly")){result.put("success",!result.containsKey("error"));write("result.json");done=true;mc.stop();return;}
    if(Files.isDirectory(Path.of("saves",WORLD)))mc.createWorldOpenFlows().openWorld(WORLD,()->{throw new IllegalStateException("Benchmark world load cancelled");});else CreateWorldScreen.openFresh(mc,mc.screen);return;
   }
   if(launched&&!form&&mc.screen instanceof CreateWorldScreen screen){form=true;creating=true;screen.getUiState().setName(WORLD);screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);screen.getUiState().setAllowCommands(true);screen.getUiState().setSeed("151126");for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().equals(Component.translatable("selectWorld.create").getString())){mc.tell(b::onPress);return;}throw new IllegalStateException("Create world button missing");}
   if(creating&&mc.player==null&&mc.screen!=null)for(var child:mc.screen.children())if(child instanceof Button b&&(b.getMessage().getString().equals("Proceed")||b.getMessage().getString().equals("I know what I'm doing!"))){mc.tell(b::onPress);return;}
   if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
   if(!preparing){preparing=true;result.put("worldEntryMs",uptime()-titleTime);mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.getSingleplayerServer().execute(()->{try{prepare(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable ex){result.put("error",ex.toString());write("failure.json");mc.execute(mc::stop);}});return;}
   if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
   if(ticks>0&&!finished){double angle=ticks*.004;double x=Math.sin(angle)*58,z=24+Math.cos(angle)*58;mc.player.setPos(x,178,z);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(0,162,26));}
   if(finished&&!inspecting){
    inspecting=true;models();shot("factory");result.put("tickMs",stats(tickTimes));result.put("warmFrameMs",stats(warmFrameTimes));result.put("frameMs",stats(frameTimes));result.put("sampleTicks",tickTimes.size());result.put("sampleFrames",frameTimes.size());result.put("achievedTps",(SAMPLE-1)/((measureEnd-measureStart)/1e9));result.put("meanFps",1000/(double)stats(frameTimes).get("mean"));result.put("usedHeapBytes",Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());write("measured.json");
    mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).teleportTo(32.5,160.2,3.8));return;
   }
   if(inspecting&&!reloading){
    mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(32.5,161.4,9.1));if(++inspectionFrames<60)return;shot("geode-closeup");reloading=true;reloadStart=uptime();
    mc.reloadResourcePacks().thenRunAsync(()->{try{models();result.put("resourceReloadMs",uptime()-reloadStart);reloadDone=true;}catch(Throwable ex){result.put("error",ex.toString());write("failure.json");done=true;mc.stop();}},mc);
   }
   if(reloadDone){mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(32.5,161.4,9.1));if(++reloadFrames<40)return;
    // Resource reload completion does not include EMI's asynchronous JEI rebuild.
    // Wait for it before disconnecting so shutdown cannot race the recipe worker.
    boolean recipesReady=(Boolean)Class.forName("dev.emi.emi.runtime.EmiReloadManager").getMethod("isLoaded").invoke(null);
    if(!recipesReady){if(reloadFrames>600)throw new IllegalStateException("EMI did not finish reloading before shutdown");return;}
    result.put("recipeReloadReady",true);models();shot("after-reload");result.put("success",!result.containsKey("error"));write("result.json");done=true;mc.stop();}
  }catch(Throwable ex){result.put("error",ex.toString());write("failure.json");done=true;mc.stop();}
 }
}
