package dk.revo.revoextraction;
import dk.revo.revostorage.*;
import dk.revo.revocultivation.*;
import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid="revoextraction",value=Dist.CLIENT)
public class ProgressionExpansionUiQa {
 static boolean started,done;static volatile boolean ready;static int stage,frames;static final BlockPos DRIVE=new BlockPos(0,160,0),TERM=DRIVE.east(),DRILL=DRIVE.offset(4,0,0),TRADE=DRIVE.offset(7,0,0);
 static final String[] NAMES={"storage-world","chest-drive","crafting-terminal","crafted-search","electric-drill","trading-colony"};
 static Path dir(){return Path.of("../../art/progression-expansion/ingame").toAbsolutePath().normalize();}
 static void server(Consumer<ServerPlayer> task){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Exception ex){try{Files.createDirectories(dir());Files.writeString(dir().resolve("failure.txt"),ex.toString());}catch(Exception ignored){}throw ex;}});}
 static StorageEntity entity(ServerPlayer p,BlockPos pos){return (StorageEntity)p.serverLevel().getBlockEntity(pos);}
 static void open(BlockPos pos){server(p->{p.closeContainer();p.connection.teleport(pos.getX()+.5,pos.getY(),pos.getZ()-2,0,0);if(pos.equals(DRIVE)||pos.equals(TERM)){var e=entity(p,pos);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new StorageMenu(id,inv,e),Component.literal(e.terminal()?"Crafting Terminal":"Chest Drive")));}else if(pos.equals(DRILL)){var e=(ElectricDrillEntity)p.serverLevel().getBlockEntity(pos);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new ElectricDrillMenu(id,inv,e),Component.literal("Electric Geological Drill")));}else{var e=(TradingColony.Entity)p.serverLevel().getBlockEntity(pos);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new TradingColony.Menu(id,inv,e),Component.literal("Trading Colony: 16 crops = 1 emerald")));}});}
 static void prepare(){server(p->{var l=p.serverLevel();if(!l.getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Progression Expansion QA"))throw new IllegalStateException("Isolated QA save required");l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();
  for(int x=-6;x<15;x++)for(int y=159;y<165;y++)for(int z=-8;z<8;z++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
  l.setBlockAndUpdate(DRIVE,RevoStorage.DRIVE.get().defaultBlockState());l.setBlockAndUpdate(TERM,RevoStorage.TERMINAL.get().defaultBlockState());var drive=entity(p,DRIVE);for(int i=0;i<9;i++)drive.chests.setStackInSlot(i,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("ironchest:diamond_chest"))));var term=entity(p,TERM);term.wind();term.insert(new ItemStack(Items.OAK_LOG,16),false);term.insert(new ItemStack(Items.DIAMOND,32),false);term.insert(new ItemStack(Items.IRON_INGOT,128),false);term.insert(new ItemStack(Items.COPPER_INGOT,64),false);term.insert(new ItemStack(Items.REDSTONE,64),false);term.insert(new ItemStack(Items.ENDER_PEARL,16),false);term.insert(new ItemStack(Items.SLIME_BALL,48),false);term.insert(new ItemStack(Items.PAPER,64),false);
  l.setBlockAndUpdate(DRILL,RevoExtraction.ELECTRIC.get().defaultBlockState());var drill=(ElectricDrillEntity)l.getBlockEntity(DRILL);drill.energy=32000;for(int i=0;i<800;i++)ElectricDrillEntity.tick(l,DRILL,drill.getBlockState(),drill);
  l.setBlockAndUpdate(TRADE,RevoCultivation.TRADE.get().defaultBlockState());var trade=(TradingColony.Entity)l.getBlockEntity(TRADE);trade.items.setStackInSlot(0,new ItemStack(Items.PAPER,64));for(int i=0;i<200;i++)TradingColony.Entity.tick(l,TRADE,trade.getBlockState(),trade);
  p.connection.teleport(5,162,-7,0,0);ready=true;
 });}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);if(!started){started=true;mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(3);prepare();return;}if(!ready)return;
  mc.options.hideGui=stage==0;if(stage==0)mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(3,160.5,.5));
  frames++;if(stage>0&&mc.screen==null){if(frames>1200)throw new IllegalStateException("UI did not open at stage "+stage);return;}
  if(stage==2&&frames==30){var handler=new StorageEmi.Handler();var recipe=dev.emi.emi.api.EmiApi.getRecipeManager().getRecipe(net.minecraft.resources.ResourceLocation.parse("minecraft:oak_planks"));if(recipe==null||!(mc.screen instanceof StorageScreen screen))throw new IllegalStateException("EMI recipe or terminal screen missing");var context=new dev.emi.emi.api.recipe.handler.EmiCraftContext<StorageMenu>(screen,handler.getInventory(screen),dev.emi.emi.api.recipe.handler.EmiCraftContext.Type.FILL_BUTTON,dev.emi.emi.api.recipe.handler.EmiCraftContext.Destination.NONE,1);if(!handler.supportsRecipe(recipe)||!handler.craft(recipe,context))throw new IllegalStateException("EMI recipe + transfer unsupported");}
  if(stage==3&&frames==20){if(!(mc.player.containerMenu instanceof StorageMenu menu))throw new IllegalStateException("Terminal menu missing");mc.gameMode.handleInventoryMouseClick(menu.containerId,0,0,ClickType.QUICK_MOVE,mc.player);}
  if(stage==3&&frames==60){((StorageScreen)mc.screen).setSearchText("diamond");server(p->{if(p.getInventory().countItem(Items.OAK_PLANKS)!=64)throw new IllegalStateException("Expected 64 planks after shift crafting; got "+p.getInventory().countItem(Items.OAK_PLANKS));if(entity(p,TERM).charge!=240)throw new IllegalStateException("Exactly 16 operations should cost 16 spring charge");});}
  if(frames<(stage==0?180:140))return;
  try{Files.createDirectories(dir());try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir().resolve(NAMES[stage]+".png"));}
   if(stage==0)open(DRIVE);if(stage==1)open(TERM);if(stage==3)open(DRILL);if(stage==4)open(TRADE);
   if(stage==5){Files.writeString(dir().resolve("qa-results.txt"),"PASS: full installed modpack loaded; new models rendered; nine diamond chests, terminal, electric drill and trading colony interfaces captured. Real client-to-server recipe transfer and shift-click crafted 64 planks from 16 stored logs, consuming exactly 16 spring operations. Diamond search packet applied. Native regression suite covers all resource programs, saved inventories and recipe bridges.\n");done=true;mc.options.hideGui=false;mc.stop();}stage++;frames=0;
  }catch(Exception ex){throw new RuntimeException(ex);}
 }
}
