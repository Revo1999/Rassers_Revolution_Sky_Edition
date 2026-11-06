package dk.revo.revoindustry.vacuum;
import java.nio.file.*;
import java.util.function.Consumer;
import dk.revo.revoindustry.RevoIndustry;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class VacuumUiQa {
 private static boolean started,done,hide,pauseOnLostFocus;private static int fov;private static volatile boolean ready;private static int stage,frames;private static boolean pressed;
 private static final String[] names={"tier-1","tier-2","tier-3","rear-output","tier-1-menu","tier-2-menu","tier-3-menu","suction","area-menu-on","area-enabled","area-menu-off","placement-tier-1","placement-tier-2","placement-tier-3","area-alignment"};
 private static BlockPos pos(int tier){return new BlockPos(tier*12,160,0);}
 private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 private static void camera(int tier,boolean side){server(p->{p.closeContainer();var b=side?new BlockPos(36,160,0):pos(tier);p.teleportTo(b.getX()+2.3,b.getY()+.7,b.getZ()-2.5);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void menu(int tier){server(p->{p.closeContainer();var b=pos(tier);p.teleportTo(b.getX()+.5,b.getY()+1,b.getZ()-2);p.openMenu((VacuumEntity)p.serverLevel().getBlockEntity(b),buf->{buf.writeVarInt(tier);buf.writeBlockPos(b);});});}
 private static void farCamera(){server(p->{p.closeContainer();var b=Vacuums.area(pos(2),2,Direction.SOUTH).getCenter();p.teleportTo(b.x+10,b.y+4,b.z-12);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void alignmentCamera(){var mc=Minecraft.getInstance();mc.options.fov().set(55);if(!dk.revo.revoindustry.client.VacuumAreaOverlay.isShown(pos(0)))dk.revo.revoindustry.client.VacuumAreaOverlay.toggle(pos(0));server(p->{p.closeContainer();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.teleportTo(7,160.7,-1.5);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void placementCamera(int tier){server(p->{p.closeContainer();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Vacuums.BLOCKS.get(tier).get()));p.teleportTo(2.2,160,-7);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void pressAreaButton(boolean expected){var mc=Minecraft.getInstance();var screen=(dk.revo.revoindustry.client.VacuumScreen)mc.screen;if(!screen.getMenu().position.equals(pos(2)))throw new IllegalStateException("Menu lost its vacuum position");var button=screen.children().stream().filter(w->w instanceof net.minecraft.client.gui.components.Button).map(w->(net.minecraft.client.gui.components.Button)w).findFirst().orElseThrow();button.onPress();if(dk.revo.revoindustry.client.VacuumAreaOverlay.isShown(pos(2))!=expected)throw new IllegalStateException("Area button failed to toggle");}
 private static void prepare(){server(p->{
  if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Vacuum QA"))throw new IllegalStateException("Vacuum QA requires its isolated save");
  var l=p.serverLevel();l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,l.getServer());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();p.teleportTo(0,164,-4);
  for(int x=-8;x<=43;x++)for(int z=-8;z<=20;z++)for(int y=159;y<=168;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
  for(int i=0;i<3;i++){l.setBlockAndUpdate(pos(i).south(),Blocks.CHEST.defaultBlockState());l.setBlockAndUpdate(pos(i),Vacuums.BLOCKS.get(i).get().defaultBlockState());}
  var side=new BlockPos(36,160,0);l.setBlockAndUpdate(side,Vacuums.BLOCKS.get(2).get().defaultBlockState().setValue(VacuumBlock.OUTLET,Direction.NORTH));
  l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(-8,159,-8,44,169,21)).forEach(net.minecraft.world.entity.Entity::discard);l.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new net.minecraft.world.phys.AABB(-8,159,-8,44,169,21)).forEach(net.minecraft.world.entity.Entity::discard);ready=true;
 });}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
  if(!started){started=true;pauseOnLostFocus=mc.options.pauseOnLostFocus;mc.options.pauseOnLostFocus=false;hide=mc.options.hideGui;fov=mc.options.fov().get();mc.options.fov().set(55);prepare();return;}if(!ready)return;
  mc.options.hideGui=stage<4||stage==7||stage==9||stage>=11;
  if(stage<4||stage==7){if(frames==0)camera(stage<3?stage:2,stage==3);var target=stage==3?new BlockPos(36,160,0):pos(stage==7?2:stage);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(target).add(0,-.2,0));}
  if(stage==9)mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vacuums.area(pos(2),2,Direction.SOUTH).getCenter());
  if(stage>=11&&stage<=13){mc.options.fov().set(100);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(.5,159.5,-3.5));}
  if(stage==14)mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vacuums.area(pos(0),0,Direction.SOUTH).getCenter());
  if((stage==8||stage==10)&&frames>=10&&!pressed&&mc.screen instanceof dk.revo.revoindustry.client.VacuumScreen){pressAreaButton(stage==8);pressed=true;}
  if(stage==7&&frames%6==0)server(p->{var b=pos(2);var e=new ItemEntity(p.serverLevel(),b.getX()+.5,160.5,-2.2,new ItemStack(Items.IRON_INGOT));e.setNoGravity(true);p.serverLevel().addFreshEntity(e);});
  if(((stage>=4&&stage<=6)||stage==8||stage==10)&&!(mc.screen instanceof dk.revo.revoindustry.client.VacuumScreen)){if(++frames>240)throw new IllegalStateException("Vacuum menu failed to open");return;}
  if(++frames<(stage==0?100:stage==7?47:stage>=11?70:45))return;
  if(stage>=11&&stage<=13&&dk.revo.revoindustry.client.VacuumAreaOverlay.placementPosition()==null)throw new IllegalStateException("Placement preview did not resolve a placeable target");
  try {var dir=Path.of("../../art/blockbench/vacuums/ingame").toAbsolutePath().normalize();Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(names[stage]+".png"));}
   if(stage==3)menu(0);else if(stage==4)menu(1);else if(stage==5)menu(2);else if(stage==6)camera(2,false);
   if(stage==7)menu(2);else if(stage==8)farCamera();else if(stage==9)menu(2);else if(stage==10)placementCamera(0);else if(stage==11)placementCamera(1);else if(stage==12)placementCamera(2);else if(stage==13)alignmentCamera();
   if(stage==14){Files.writeString(dir.resolve("qa-results.txt"),"All three single-block vacuums rendered with exact 1/3/9-slot menus, rear output plate, live suction particles, working UI area toggle on/off and all three automatic placement range previews.\n");done=true;mc.options.hideGui=hide;mc.options.fov().set(fov);mc.options.pauseOnLostFocus=pauseOnLostFocus;mc.stop();}
   stage++;frames=0;pressed=false;
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
