package dk.revo.revoextraction;
import dk.revo.revocultivation.*;
import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=RevoExtraction.MOD_ID,value=Dist.CLIENT)
public final class ProgressionBridgeUiQa {
 private static boolean started,done,hide,pause;private static volatile boolean ready;private static int fov,stage,frames;
 private static final String[] names={"cow-world","blaze-world","cow-output","blaze-output","flux-dust"};
 private static BlockPos pos(int i){return new BlockPos(i*8,160,0);}
 private static void server(Consumer<ServerPlayer> task){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 private static FarmMachine farm(ServerPlayer p,int i){return (FarmMachine)p.serverLevel().getBlockEntity(pos(i));}
 private static void camera(int i){server(p->{p.closeContainer();var b=pos(i);p.connection.teleport(b.getX()+4,b.getY()+1.7,-4.5,0,0);p.getAbilities().flying=true;p.onUpdateAbilities();});}
 private static void open(int i){server(p->{p.closeContainer();var m=farm(p,i);var b=m.at(1,0,0);p.connection.teleport(b.getX()+1.5,b.getY()+.5,b.getZ()-2,0,0);var hatch=(FarmHatchEntity)p.serverLevel().getBlockEntity(b);p.openMenu(new SimpleMenuProvider((id,inv,viewer)->new FarmHatchMenu(id,inv,hatch,m),Component.translatable("gui.revocultivation.hatch")),buf->buf.writeEnum(m.kind()));});}
 private static void openFlux(){server(p->{p.closeContainer();var m=(DrillMachine)p.serverLevel().getBlockEntity(pos(2));var b=m.at(-1,0,0);p.connection.teleport(b.getX()-1.5,b.getY()+.5,b.getZ()-2,0,0);var hatch=(HatchEntity)p.serverLevel().getBlockEntity(b);p.openMenu(new SimpleMenuProvider((id,inv,viewer)->new DrillHatchMenu(id,inv,hatch,m),Component.translatable("gui.revoextraction.item_output_hatch")));});}
 private static void prepare(){server(p->{
  if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Resource Bridge QA"))throw new IllegalStateException("Dedicated copied QA save required");var l=p.serverLevel();l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();
  for(int x=-6;x<=24;x++)for(int y=159;y<=168;y++)for(int z=-8;z<=8;z++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
  for(int i=0;i<2;i++){var kind=i==0?MachineKind.HARVESTER:MachineKind.STEEL_HARVESTER;l.setBlockAndUpdate(pos(i),kind.controller().defaultBlockState());var m=farm(p,i);
   for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){var part=kind.part(x,y,z);var s=kind.block(part).defaultBlockState();if(part==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,Direction.NORTH);if(part==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,Direction.EAST);if(part==MachineKind.Part.FILTER)s=s.setValue(FarmFilter.FACING,Direction.NORTH).setValue(FarmFilter.SPECIES,i==0?3:4);l.setBlockAndUpdate(m.at(x,y,z),s);}
   if(!m.checkStructure())throw new IllegalStateException("Farm fixture did not form");for(int t=0;t<kind.ticks;t++)FarmMachine.tick(l,m.getBlockPos(),m.getBlockState(),m);
  }
  var tier=DrillTier.IRON;l.setBlockAndUpdate(pos(2),RevoExtraction.IRON_CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,Direction.NORTH));var drill=(DrillMachine)l.getBlockEntity(pos(2));
  for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){var part=tier.part(x,y,z);var s=tier.block(part).defaultBlockState();if(part==DrillLayout.Part.CONTROLLER)s=s.setValue(DrillController.FACING,Direction.NORTH);if(part==DrillLayout.Part.INPUT)s=s.setValue(DrillInput.FACING,Direction.EAST);if(part==DrillLayout.Part.HATCH)s=s.setValue(DrillHatch.FACING,Direction.WEST);l.setBlockAndUpdate(drill.at(x,y,z),s);}
  if(!drill.checkStructure())throw new IllegalStateException("Drill fixture did not form");drill.getPersistentData().putInt("cycle",127);var drive=(DrillDrive)l.getBlockEntity(drill.at(1,1,0));drive.setSpeed(32);for(int t=0;t<19;t++)DrillMachine.tick(l,drill.getBlockPos(),drill.getBlockState(),drill);drive.setSpeed(0);
  l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(-6,159,-8,25,169,9)).forEach(net.minecraft.world.entity.Entity::discard);ready=true;
 });}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
  if(!started){started=true;hide=mc.options.hideGui;pause=mc.options.pauseOnLostFocus;fov=mc.options.fov().get();mc.options.pauseOnLostFocus=false;mc.options.fov().set(55);prepare();return;}if(!ready)return;mc.options.hideGui=stage<2;
  if(stage<2){if(frames==0)camera(stage);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(pos(stage)).add(0,1,1));}
  if(stage>=2&&mc.screen==null){if(++frames>200)throw new IllegalStateException("Output menu failed to open");return;}
  if(++frames<(stage==0?100:45))return;
  try{var dir=Path.of("../../art/progression-bridges/ingame").toAbsolutePath().normalize();Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(names[stage]+".png"));}
   if(stage==1)open(0);else if(stage==2)open(1);else if(stage==3)openFlux();
   if(stage==4){Files.writeString(dir.resolve("qa-results.txt"),"Cow and Blaze displayed in their formed machines; leather/beef, blaze rod and real rare Flux Dust output menus captured in isolated save.\n");done=true;mc.options.hideGui=hide;mc.options.pauseOnLostFocus=pause;mc.options.fov().set(fov);mc.stop();}stage++;frames=0;
  }catch(Exception ex){throw new RuntimeException(ex);}
 }
}
