package dk.revo.revocultivation;
import java.nio.file.*;
import java.util.function.Consumer;
import dk.revo.revocultivation.client.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=RevoCultivation.MOD_ID,value=Dist.CLIENT)
public class SmallUiQa {
 static boolean started,done;static volatile boolean ready;static int stage=-7,frames,total;
 static final BlockPos A=new BlockPos(0,128,0),B=new BlockPos(3,128,0);
 static final Path OUT=Path.of("../../art/blockbench/small-cultivation/ingame").toAbsolutePath().normalize();
 static void server(Consumer<ServerPlayer> f){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->f.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 static void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
 static void open(boolean silk){server(p->{p.closeContainer();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var pos=silk?B:A;p.teleportTo(pos.getX()+.5,pos.getY()+.4,pos.getZ()-2.5);p.gameMode.useItemOn(p,p.serverLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));});}
 static void feed(){var mc=Minecraft.getInstance();mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(B),Direction.NORTH,B,false));}
 static void snapshot(String n){try{Files.createDirectories(OUT);try(var img=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){img.writeToFile(OUT.resolve(n+".png"));}}catch(Exception e){throw new RuntimeException(e);}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!started){started=true;server(p->{check(p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Single Block QA"),"Requires copied QA world");var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.teleportTo(1.7,129,-3);for(int x=-5;x<9;x++)for(int z=-6;z<6;z++)for(int y=127;y<134;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==127?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());l.setDayTime(1000);l.setBlockAndUpdate(A,SmallMachines.COMPOSTER.get().defaultBlockState());l.setBlockAndUpdate(B,SmallMachines.CULTIVATOR.get().defaultBlockState());var a=(SmallMachineEntity)l.getBlockEntity(A);var b=(SmallMachineEntity)l.getBlockEntity(B);a.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,4));p.getInventory().clearContent();l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(-8,127,-9,10,140,11)).forEach(net.minecraft.world.entity.Entity::discard);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.OAK_LEAVES,2));p.teleportTo(5.1,129,-2.5);p.getAbilities().flying=true;p.onUpdateAbilities();ready=true;});return;}
  if(!ready)return;if(++total>1800)throw new IllegalStateException("Small QA timeout stage "+stage);
  if(stage<0){
   var m=(SmallMachineEntity)mc.level.getBlockEntity(B);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(B).add(0,.2,0));frames++;
   if(stage==-7&&frames>=45){check(m.fillLevel()==0&&m.visualStage()==0,"Empty contents synchronized");snapshot("cultivator-empty");feed();stage++;frames=0;return;}
   if(stage==-6&&frames>=45){check(m.fillLevel()==4&&m.storage.getStackInSlot(0).getCount()==1&&m.renderedFill(1)>.49f,"First real hand interaction raises half-full contents");snapshot("cultivator-half");feed();stage++;frames=0;return;}
   if(stage==-5&&frames>=45){check(m.fillLevel()==8&&m.progress()==0,"Full tray waits for starter");snapshot("cultivator-full");server(p->p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SmallMachines.WORM.get())));stage++;frames=0;return;}
   if(stage==-4){if(frames==10)feed();if(frames>=220){check(m.visualStage()==2&&m.processFraction()>.45f&&m.processFraction()<.8f,"Real synchronized work drives leaf-to-silk surface");snapshot("cultivator-working");stage++;frames=0;}return;}
   if(stage==-3&&frames>=240){check(m.visualStage()==3&&m.storage.getStackInSlot(2).getCount()==2,"Ready cocoons remain visible");snapshot("cultivator-ready");open(true);stage++;frames=0;return;}
   if(stage==-2){if(!(mc.screen instanceof SmallMachineScreen))return;if(frames==10)mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,2,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,mc.player);if(frames>=30){mc.player.closeContainer();stage++;frames=0;}return;}
   if(stage==-1&&frames>=50){check(m.fillLevel()==0&&m.visualStage()==0&&m.storage.getStackInSlot(2).isEmpty(),"Actual menu collection clears contents");snapshot("cultivator-collected");server(p->{var b=(SmallMachineEntity)p.serverLevel().getBlockEntity(B);b.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,32));for(int i=0;i<410;i++)b.tick();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.teleportTo(1.7,129,-3);});stage++;frames=0;return;}
   return;
  }
  if(stage==0){mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(1.7,128.6,.5));if(++frames<75)return;snapshot("machines-world");open(false);stage++;frames=0;return;}
  if(stage==1){if(!(mc.screen instanceof SmallMachineScreen s))return;check(s.getMenu().slots.size()==39,"Vanilla player inventory slots");check(s.getMenu().slots.get(3).y==102&&s.getMenu().slots.get(30).y==160,"MI/vanilla inventory layout");if(++frames<40)return;snapshot("composter-ui");check(s.getMenu().getSlot(2).getItem().is(SmallMachines.COMPOST.get()),"Automatic passive timer produces real compost");open(true);stage++;frames=0;return;}
  if(stage==2){if(!(mc.screen instanceof SmallMachineScreen s)||!s.getMenu().silk)return;if(++frames<45)return;check(s.getMenu().getSlot(1).getItem().is(SmallMachines.WORM.get())&&s.getMenu().getSlot(2).getItem().is(SmallMachines.COCOON.get()),"Cultivator inventory synchronized");snapshot("silkworm-ui");SmallMachineEmi.open(false);stage++;frames=0;return;}
  if(stage==3||stage==4){if(mc.screen==null)return;if(++frames<45)return;snapshot(stage==3?"composter-emi":"silkworm-emi");if(stage==3){SmallMachineEmi.open(true);stage++;frames=0;return;}mc.player.closeContainer();server(p->{p.teleportTo(.5,128,-3);});stage++;frames=0;return;}
  if(stage==5){if(mc.screen!=null)return;mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A));if(++frames<65)return;var data=snownee.jade.impl.WailaClientRegistration.instance().getServerData().getIntArray("RevoSmall");check(data.length==3,"Real Jade server status payload");snapshot("jade-status");try{Files.writeString(OUT.resolve("qa-results.txt"),"PASS: full-pack client; native one-block models; actual useItemOn menus; automatic timed compost production without power; synchronized retained worm and cocoons; vanilla/MI inventory rows; both EMI production pages; real Jade server data; actual network hand-feeding raises empty/half/full contents; real elapsed cycle shows silk and growing cocoons; ready output and menu collection clear the rendered contents.\n");}catch(Exception ex){throw new RuntimeException(ex);}done=true;mc.stop();}
 }
}

