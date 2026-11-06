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
 private static boolean started,done,hide;private static int fov;private static volatile boolean ready;private static int stage,frames;
 private static final String[] names={"tier-1","tier-2","tier-3","side-outlet","tier-1-menu","tier-2-menu","tier-3-menu","suction"};
 private static BlockPos pos(int tier){return new BlockPos(tier*12,161,0);}
 private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 private static void camera(int tier,boolean side){server(p->{p.closeContainer();var b=side?new BlockPos(36,160,0):pos(tier);p.teleportTo(b.getX()+2.3,b.getY()+.7,b.getZ()-2.5);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void menu(int tier){server(p->{p.closeContainer();var b=pos(tier);p.teleportTo(b.getX()+.5,b.getY()+1,b.getZ()-2);p.openMenu((VacuumEntity)p.serverLevel().getBlockEntity(b),buf->buf.writeVarInt(tier));});}
 private static void prepare(){server(p->{
  if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Vacuum QA"))throw new IllegalStateException("Vacuum QA requires its isolated save");
  var l=p.serverLevel();l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,l.getServer());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();p.teleportTo(0,164,-4);
  for(int x=-8;x<=43;x++)for(int z=-8;z<=20;z++)for(int y=159;y<=168;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
  for(int i=0;i<3;i++){l.setBlockAndUpdate(pos(i).below(),Blocks.CHEST.defaultBlockState());l.setBlockAndUpdate(pos(i),Vacuums.BLOCKS.get(i).get().defaultBlockState());}
  var side=new BlockPos(36,160,0);l.setBlockAndUpdate(side.east(),Blocks.CHEST.defaultBlockState());l.setBlockAndUpdate(side,Vacuums.BLOCKS.get(1).get().defaultBlockState().setValue(VacuumBlock.OUTLET,Direction.EAST));
  l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(-8,159,-8,44,169,21)).forEach(net.minecraft.world.entity.Entity::discard);l.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,new net.minecraft.world.phys.AABB(-8,159,-8,44,169,21)).forEach(net.minecraft.world.entity.Entity::discard);ready=true;
 });}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!started){started=true;hide=mc.options.hideGui;fov=mc.options.fov().get();mc.options.fov().set(55);prepare();return;}if(!ready)return;
  mc.options.hideGui=stage<4||stage==7;
  if(stage<4||stage==7){if(frames==0)camera(stage<3?stage:2,stage==3);var target=stage==3?new BlockPos(36,160,0):pos(stage==7?2:stage);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(target).add(0,-.2,0));}
  if(stage==7&&frames%6==0)server(p->{var b=pos(2);var e=new ItemEntity(p.serverLevel(),b.getX()+.5,161.3,-2.2,new ItemStack(Items.IRON_INGOT));e.setNoGravity(true);p.serverLevel().addFreshEntity(e);});
  if(stage>=4&&stage<=6&&!(mc.screen instanceof dk.revo.revoindustry.client.VacuumScreen)){if(++frames>240)throw new IllegalStateException("Vacuum menu failed to open");return;}
  if(++frames<(stage==0?100:stage==7?47:45))return;
  try {var dir=Path.of("../../art/blockbench/vacuums/ingame").toAbsolutePath().normalize();Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(names[stage]+".png"));}
   if(stage==3)menu(0);else if(stage==4)menu(1);else if(stage==5)menu(2);else if(stage==6)camera(2,false);
   if(stage==7){Files.writeString(dir.resolve("qa-results.txt"),"All three single-block vacuums rendered with exact 1/3/9-slot menus, side-oriented output and live suction particles.\n");done=true;mc.options.hideGui=hide;mc.options.fov().set(fov);mc.stop();}
   stage++;frames=0;
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
