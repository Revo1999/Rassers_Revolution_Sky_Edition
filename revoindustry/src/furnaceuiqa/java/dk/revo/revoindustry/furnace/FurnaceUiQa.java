package dk.revo.revoindustry.furnace;
import java.nio.file.*;
import java.util.function.Consumer;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.client.FurnaceEmiPlugin;
import dk.revo.revolib.client.guide.MultiblockGuideScreen;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class FurnaceUiQa {
 private static boolean started,done;private static volatile boolean ready;private static int stage,frames;
 private static final FurnaceTier[] tiers=FurnaceTier.values();
 private static final String[] names={"copper-world","iron-world","silver-world","gold-world","diamond-world","emerald-world","obsidian-world","crystal-world","netherite-world","rainbow-world","controller","item-hatch","liquid-hatch","gas-hatch","emi-guide"};
 private static boolean originalHideGui;
 private static BlockPos origin(int i){return new BlockPos(i%5*7,160,i/5*7);}
 private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 private static FurnaceController machine(ServerPlayer p,int i){return (FurnaceController)p.serverLevel().getBlockEntity(origin(i));}
 private static BlockPos at(FurnaceController m,int x,int y,int z){return ShapeMatcher.toWorldPos(m.getBlockPos(),Direction.NORTH,new BlockPos(x,y,z));}
 private static void camera(int i){server(p->{p.closeContainer();var pos=origin(i);p.teleportTo(pos.getX()+3.8,pos.getY()+1.7,pos.getZ()-4.3);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void open(int i,int kind){server(p->{var m=machine(p,i);var pos=kind==0?m.getBlockPos():kind==1?at(m,-1,0,0):kind==2?at(m,1,0,1):at(m,-1,0,1);p.teleportTo(pos.getX()+.5,pos.getY()+1,pos.getZ()-2);((MachineBlockEntity)p.serverLevel().getBlockEntity(pos)).openMenu(p);});}
 private static void prepare(){server(p->{
  if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Furnace QA"))throw new IllegalStateException("Furnace QA requires its isolated copied save");
  var level=p.serverLevel();level.setDayTime(1000);level.setWeatherParameters(100000,0,false,false);level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,level.getServer());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();p.teleportTo(0,164,-6);
  for(int x=-5;x<=34;x++)for(int z=-8;z<=17;z++)for(int y=159;y<=168;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
  for(int i=0;i<tiers.length;i++){
   var tier=tiers[i];var pos=origin(i);level.setBlockAndUpdate(pos,Furnaces.CONTROLLERS.get(tier).get().defaultBlockState());var m=machine(p,i);m.orientation.facingDirection=Direction.NORTH;
   for(var entry:m.getActiveShape().simpleMembers.entrySet())level.setBlockAndUpdate(ShapeMatcher.toWorldPos(pos,Direction.NORTH,entry.getKey()),entry.getValue().getPreviewState());
   level.setBlockAndUpdate(at(m,-1,0,0),Furnaces.ITEM_HATCHES.get(tier.factoryTier*2).get().defaultBlockState());level.setBlockAndUpdate(at(m,1,0,0),Furnaces.ITEM_HATCHES.get(tier.factoryTier*2+1).get().defaultBlockState());
   level.setBlockAndUpdate(at(m,0,0,2),Furnaces.FUEL_HATCHES.get(tier.factoryTier*3).get().defaultBlockState());
   level.setBlockAndUpdate(at(m,1,0,1),Furnaces.FUEL_HATCHES.get(tier.factoryTier*3+1).get().defaultBlockState());level.setBlockAndUpdate(at(m,-1,0,1),Furnaces.FUEL_HATCHES.get(tier.factoryTier*3+2).get().defaultBlockState());
   m.tick();if(!m.isShapeValid())throw new IllegalStateException("Visual fixture did not form: "+tier);
  }level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(-5,159,-8,35,169,18)).forEach(net.minecraft.world.entity.Entity::discard);level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new net.minecraft.world.phys.AABB(-5,159,-8,35,169,18)).forEach(net.minecraft.world.entity.Entity::discard);ready=true;
 });}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!started){started=true;originalHideGui=mc.options.hideGui;prepare();return;}if(!ready)return;
  if(frames%20==0)server(p->{for(int i=0;i<tiers.length;i++){var m=machine(p,i);var input=((MachineBlockEntity)p.serverLevel().getBlockEntity(at(m,-1,0,0))).getInventory();var slot=input.getItemStacks().getFirst();slot.setKey(ItemVariant.of(Items.RAW_IRON));slot.setAmount(64);for(var out:((MachineBlockEntity)p.serverLevel().getBlockEntity(at(m,1,0,0))).getInventory().getItemStacks())out.empty();((FurnaceFuelHatch)p.serverLevel().getBlockEntity(at(m,0,0,2))).items().insertItem(0,new ItemStack(Items.COAL,64),false);}});
  mc.options.hideGui=stage<10;
  if(stage<10){if(frames==0)camera(stage);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(origin(stage)).add(0,1,1));}
  if(stage>=10&&stage<=13&&!(mc.screen instanceof aztech.modern_industrialization.client.machines.gui.MachineScreen)){if(++frames>240)throw new IllegalStateException("Furnace QA menu never opened at stage "+stage);return;}
  if(++frames<(stage==0?100:45))return;
  try{var dir=Path.of("../../art/blockbench/furnaces/ingame").toAbsolutePath().normalize();Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(names[stage]+".png"));}
   if(stage<9)camera(stage+1);
   else if(stage==9)open(FurnaceTier.IRON.ordinal(),0);
   else if(stage==10)open(FurnaceTier.EMERALD.ordinal(),1);
   else if(stage==11)open(FurnaceTier.EMERALD.ordinal(),2);
   else if(stage==12)open(FurnaceTier.EMERALD.ordinal(),3);
   else if(stage==13){mc.player.closeContainer();var guide=FurnaceEmiPlugin.guide(FurnaceTier.RAINBOW);mc.setScreen(new MultiblockGuideScreen(null,new dk.revo.revolib.client.guide.MultiblockPreview(guide),()->{}));}
   else if(stage==14){Files.writeString(dir.resolve("qa-results.txt"),"All ten furnace tiers built and rendered in-game; controller/hatch native MI menus and casing-adapted assembly guide captured in an isolated QA save.\n");done=true;mc.options.hideGui=originalHideGui;mc.stop();}
   stage++;frames=0;
  }catch(Exception ex){throw new RuntimeException(ex);}
 }
}
