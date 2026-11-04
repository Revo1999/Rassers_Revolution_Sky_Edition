package dk.revo.revoindustry;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public class ChapterOneUiQa{
 static final Path OUT=Path.of("../../art/quests/ingame").toAbsolutePath().normalize();
 static final long CHAPTER=0x5E10000000000000L,TIPS=0x5E1F000000000000L;
 static final BlockPos RAIN=new BlockPos(0,129,0),CRUCIBLE=new BlockPos(3,129,0);
 static boolean launched,form,created,started,done;static volatile boolean ready;static int stage,frames,total;static String world;
 static void check(boolean yes,String text){if(!yes)throw new IllegalStateException(text);}
 static void server(Consumer<ServerPlayer> f){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->f.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
 static void note(String s){try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("qa-results.txt"),s+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}}
 static void shot(String s){try{Files.createDirectories(OUT);try(var img=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){img.writeToFile(OUT.resolve(s+".png"));}}catch(Exception e){throw new RuntimeException(e);}}
 static void equip(ServerPlayer p,Item item){for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item)){if(i==p.getInventory().selected)return;var s=p.getInventory().removeItem(i,1);var old=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,s);if(!old.isEmpty())p.getInventory().placeItemBackInInventory(old);return;}throw new IllegalStateException("Missing claimed manual "+BuiltInRegistries.ITEM.getKey(item));}
 static void open(boolean rain){server(p->{p.closeContainer();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var pos=rain?RAIN:CRUCIBLE;p.teleportTo(pos.getX()+.5,130,pos.getZ()-2.5);p.gameMode.useItemOn(p,p.serverLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));});}
 static void claim(ServerPlayer p,long qid){var file=ServerQuestFile.INSTANCE;var q=file.getQuest(qid);check(q!=null,"Missing quest "+Long.toHexString(qid));var data=file.getOrCreateTeamData(p);for(var task:q.getTasks())data.setProgress(task,task.getMaxProgress());for(var reward:q.getRewards())if(!data.isRewardClaimed(p.getUUID(),reward))data.claimReward(p,reward,true);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.getOverlay()!=null)return;
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null){
   if(!launched&&mc.screen instanceof TitleScreen){launched=true;world="Revo First Chapter QA";int n=1;while(Files.exists(Path.of("saves",world)))world="Revo First Chapter QA "+(++n);CreateWorldScreen.openFresh(mc,mc.screen);return;}
   if(launched&&!form&&mc.screen instanceof CreateWorldScreen s){form=true;s.getUiState().setName(world);s.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);s.getUiState().setAllowCommands(false);s.getUiState().setSeed("112026");check(s.getUiState().getWorldType().preset().is(ResourceKey.create(Registries.WORLD_PRESET,ResourceLocation.fromNamespaceAndPath("skyblockbuilder","skyblock"))),"New world must default to the Skyblock preset");for(var w:s.children())if(w instanceof Button b&&b.getMessage().getString().equals(Component.translatable("selectWorld.create").getString())){created=true;mc.tell(b::onPress);return;}throw new IllegalStateException("Create-world button missing");}
   if(created&&mc.screen!=null){for(var w:mc.screen.children())if(w instanceof Button b&&(b.getMessage().getString().equals("Proceed")||b.getMessage().getString().equals("I know what I'm doing!"))){mc.tell(b::onPress);break;}}
   return;
  }
  if(!started){if(++frames<120)return;started=true;frames=0;try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("qa-results.txt"),"Fresh world: "+world+"\n");}catch(Exception ex){throw new RuntimeException(ex);}server(p->{check(p.getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo First Chapter QA"),"Only a new named QA save may be modified");int count=0;for(var s:p.getInventory().items)if(!s.isEmpty()){check(s.is(PrimitiveMachines.BOOK.get())&&s.getCount()==1,"Starting inventory must contain only the Revolution Book: "+s);count+=s.getCount();}check(count==1,"Exactly one starting book required");check(p.serverLevel().getBlockState(new BlockPos(9,69,6)).is(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("craftingstation","crafting_station"))),"Placed crafting station missing");check(!p.serverLevel().getBlockState(new BlockPos(9,69,7)).is(Blocks.CHEST),"No starter chest may bypass the resource chain");check(!p.hasPermissions(2),"Opening progression must work without operator permission");for(int i=1;i<=35;i++)check(ServerQuestFile.INSTANCE.getQuest(CHAPTER+i)!=null,"Chapter quest missing "+i);for(int i=1;i<=6;i++)check(ServerQuestFile.INSTANCE.getQuest(TIPS+i)!=null,"Tips quest missing "+i);note("PASS: actual fresh Skyblock world; placed crafting station; only one starting Revolution Book; no free starter chest; non-op player; all 35 first-chapter quests and six Tips entries loaded.");ready=true;});return;}
  if(!ready)return;if(++total>3500)throw new IllegalStateException("Chapter QA timeout stage "+stage+" screen="+mc.screen);
  frames++;
  switch(stage){
   case 0->{if(frames<10)return;shot("starting-island");mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);stage++;frames=0;}
   case 1->{if(mc.screen==null||!ClientQuestFile.exists())return;if(frames<30)return;ClientQuestFile.openBookToQuestObject(CHAPTER);stage++;frames=0;}
   case 2->{if(frames<35)return;shot("chapter-one");note("PASS: actual Revolution Book use opened FTB Quests.");ClientQuestFile.openBookToQuestObject(TIPS);stage++;frames=0;}
   case 3->{if(frames<35)return;shot("tips-guides");mc.player.closeContainer();server(p->{for(long n:new long[]{1,3,4,5,6,18})claim(p,CHAPTER+n);check(p.serverLevel().getLevelData().isRaining(),"Rain Barrel quest command must start first shower for a non-op player");for(int i=1;i<=4;i++)claim(p,TIPS+i);check(p.getInventory().countItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("ae2:guide")))>0&&p.getInventory().countItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("modern_industrialization:guidebook")))>0,"Free manual rewards must be actual registered books");note("PASS: real FTB reward pipeline granted ungated manuals; first-barrel command started rain for a non-op player.");equip(p,BuiltInRegistries.ITEM.get(ResourceLocation.parse("ae2:guide")));});stage++;frames=0;}
   case 4->{if(frames==15)mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);if(frames<45||mc.screen==null)return;shot("ae2-manual");note("PASS: actual AE2 manual opened "+mc.screen.getClass().getName());mc.player.closeContainer();server(p->equip(p,BuiltInRegistries.ITEM.get(ResourceLocation.parse("modern_industrialization:guidebook"))));stage++;frames=0;}
   case 5->{if(frames==15)mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);if(frames<45||mc.screen==null)return;shot("mi-manual");note("PASS: actual MI manual opened "+mc.screen.getClass().getName());mc.player.closeContainer();server(p->{var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);for(int x=-4;x<=8;x++)for(int z=-5;z<=6;z++)l.setBlockAndUpdate(new BlockPos(x,127,z),Blocks.STONE_BRICKS.defaultBlockState());l.setBlockAndUpdate(RAIN.below(),Blocks.OAK_PLANKS.defaultBlockState());l.setBlockAndUpdate(CRUCIBLE.below(),Blocks.TORCH.defaultBlockState());l.setBlockAndUpdate(RAIN,PrimitiveMachines.BARREL.get().defaultBlockState());l.setBlockAndUpdate(CRUCIBLE,PrimitiveMachines.CRUCIBLE.get().defaultBlockState());((PrimitiveVesselEntity)l.getBlockEntity(CRUCIBLE)).inventory.setStackInSlot(0,new ItemStack(Items.COBBLESTONE,4));p.teleportTo(2.1,130,-3.7);p.getAbilities().flying=true;p.onUpdateAbilities();});stage++;frames=0;}
   case 6->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(1.8,129.8,.5));if(frames<240)return;var r=(PrimitiveVesselEntity)mc.level.getBlockEntity(RAIN);var c=(PrimitiveVesselEntity)mc.level.getBlockEntity(CRUCIBLE);check(r.tank.getFluidAmount()==1000&&c.tank.getFluidAmount()>=250,"Real exposed rain and torch heat must advance both vessels");shot("rain-and-crucible");note("PASS: real rain filled the exposed barrel and real torch heat melted cobblestone over elapsed server ticks.");open(true);stage++;frames=0;}
   case 7->{if(!(mc.screen instanceof dk.revo.revoindustry.client.PrimitiveVesselScreen s)||!s.getMenu().rain)return;if(frames<35)return;check(s.getMenu().value(0)==1000&&s.getMenu().slots.size()==38&&s.getMenu().slots.get(2).y==102,"Rain menu synchronization and standard inventory");shot("rain-barrel-ui");open(false);stage++;frames=0;}
   case 8->{if(!(mc.screen instanceof dk.revo.revoindustry.client.PrimitiveVesselScreen s)||s.getMenu().rain)return;if(frames<35)return;check(s.getMenu().value(0)>=250&&s.getMenu().value(3)==1,"Crucible GUI must report actual lava and torch heat");shot("crucible-ui");mc.player.closeContainer();server(p->{var m=(PrimitiveVesselEntity)p.serverLevel().getBlockEntity(RAIN);m.inventory.setStackInSlot(0,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("revoextraction:dust"))));check(m.mix(),"Full rain barrel mixes actual registered dust");check(m.inventory.getStackInSlot(1).is(Items.CLAY),"Clay result exists");p.teleportTo(.5,130,-2.5);});stage++;frames=0;}
   case 9->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(RAIN));if(frames<40)return;shot("clay-result");open(true);stage++;frames=0;}
   case 10->{if(!(mc.screen instanceof dk.revo.revoindustry.client.PrimitiveVesselScreen s))return;if(frames<35)return;check(s.getMenu().getSlot(1).getItem().is(Items.CLAY),"Actual menu contains crafted clay");shot("clay-ui");note("PASS: actual dust-to-clay mixing, synchronized vessel menus and standard inventory layout.");mc.player.closeContainer();stage++;frames=0;}
   case 11->{if(frames<50)return;var data=snownee.jade.impl.WailaClientRegistration.instance().getServerData().getIntArray("RevoVessel");check(data.length==4,"Actual Jade vessel payload");shot("vessel-jade");note("PASS: real Jade server payload. Fresh-world quest and primitive machine QA complete.");done=true;mc.stop();}
  }
 }
}

