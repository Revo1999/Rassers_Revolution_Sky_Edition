package dk.revo.revoextraction;

import dk.revo.revolib.*;
import dk.revo.revolib.client.MachineAudio;
import dk.revo.revoindustry.furnace.*;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Optional real-client sound-engine test, with real furnace/drill work and synthetic crowd load. */
@EventBusSubscriber(modid="revoextraction",value=Dist.CLIENT)
public final class MachineAudioQa {
    static final BlockPos BASE=new BlockPos(2100,200,2100);
    static final Path OUT=Path.of("../../screenshots/audio-check").toAbsolutePath().normalize();
    static volatile boolean ready;static volatile int ticks;static boolean started,done,oldPause;
    static ServerLevel level;static FurnaceController furnace;static DrillMachine drill;
    static final List<BlockPos> crowd=new ArrayList<>();static final List<String> evidence=new ArrayList<>();
    static final Set<Integer> checked=new HashSet<>();
    static BlockPos at(int x,int y,int z){return BASE.offset(x,y,z);}
    static BlockPos port(int x,int y,int z){return ShapeMatcher.toWorldPos(furnace.getBlockPos(),Direction.SOUTH,new BlockPos(x,y,z));}
    static void check(boolean condition,String message){if(!condition)throw new IllegalStateException(message);evidence.add("PASS: "+message);System.out.println("AUDIO QA PASS: "+message);}
    static void build(){
        var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();
            if(!server.getWorldPath(LevelResource.ROOT).toString().contains("Revo Audio QA"))throw new IllegalStateException("Isolated audio QA save required");
            level=server.overworld();
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)level.setChunkForced((BASE.getX()>>4)+x,(BASE.getZ()>>4)+z,true);
            for(int x=-8;x<=12;x++)for(int z=-7;z<=12;z++)for(int y=-2;y<=7;y++)level.setBlock(at(x,y,z),(y==-2?Blocks.STONE_BRICKS:Blocks.AIR).defaultBlockState(),2);
            level.setBlockAndUpdate(BASE,Furnaces.CONTROLLERS.get(FurnaceTier.IRON).get().defaultBlockState());furnace=(FurnaceController)level.getBlockEntity(BASE);furnace.orientation.facingDirection=Direction.SOUTH;
            for(var member:furnace.getActiveShape().simpleMembers.entrySet())level.setBlockAndUpdate(ShapeMatcher.toWorldPos(BASE,Direction.SOUTH,member.getKey()),member.getValue().getPreviewState());
            level.setBlockAndUpdate(port(-1,0,0),Furnaces.ITEM_HATCHES.get(0).get().defaultBlockState());
            level.setBlockAndUpdate(port(1,0,0),Furnaces.ITEM_HATCHES.get(1).get().defaultBlockState());
            level.setBlockAndUpdate(port(0,0,2),Furnaces.FUEL_HATCHES.get(0).get().defaultBlockState());
            for(int x:new int[]{-1,1}){var hatch=(FurnaceItemHatch)level.getBlockEntity(port(x,0,0));hatch.orientation.outputDirection=Direction.SOUTH;hatch.sync(false);}
            var fuel=(FurnaceFuelHatch)level.getBlockEntity(port(0,0,2));fuel.orientation.outputDirection=Direction.NORTH;fuel.items().insertItem(0,new ItemStack(Items.COAL,64),false);
            ((FurnaceItemHatch)level.getBlockEntity(port(-1,0,0))).getInventory().itemStorage.itemHandler.insertItem(0,new ItemStack(Items.RAW_IRON,64),false);
            var pos=at(6,1,0);level.setBlockAndUpdate(pos,RevoExtraction.IRON_CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,Direction.SOUTH));drill=(DrillMachine)level.getBlockEntity(pos);
            for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
                var type=DrillTier.IRON.part(x,y,z);var state=DrillTier.IRON.block(type).defaultBlockState();
                if(type==DrillLayout.Part.CONTROLLER)state=state.setValue(DrillController.FACING,Direction.SOUTH);
                if(type==DrillLayout.Part.INPUT)state=state.setValue(DrillInput.FACING,Direction.WEST);
                if(type==DrillLayout.Part.HATCH)state=state.setValue(DrillHatch.FACING,Direction.EAST);
                level.setBlockAndUpdate(drill.at(x,y,z),state);
            }
            var motorPos=drill.at(1,1,0).west();level.setBlockAndUpdate(motorPos,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
            var motor=(CreativeMotorBlockEntity)level.getBlockEntity(motorPos);motor.generatedSpeed.setValue(64);motor.updateGeneratedRotation();
            for(int i=0;i<12;i++){var p=at(-5+(i%6)*2,0,6+(i/6)*2);level.setBlockAndUpdate(p,Blocks.BARREL.defaultBlockState());crowd.add(p);}
            var player=server.getPlayerList().getPlayer(mc.player.getUUID());player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);player.connection.teleport(BASE.getX()+3,BASE.getY()+1.8,BASE.getZ()+5,180,15);level.setDayTime(6000);ready=true;
        });
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event){
        if(!ready||done)return;ticks++;
        if(ticks>=80&&ticks<145)for(var pos:crowd)MachineFeedback.work(level,pos,MachineFeedback.Voice.CONVERTER);
        if(ticks==220){
            for(int i=0;i<drill.inventory.getSlots();i++)drill.inventory.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
            var out=((FurnaceItemHatch)level.getBlockEntity(port(1,0,0))).getInventory().getItemStacks().getFirst();out.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(Items.STONE));out.setAmount(64);
        }
        if(ticks>=300&&ticks<320)MachineFeedback.work(level,crowd.getFirst(),MachineFeedback.Voice.FURNACE_HEAVY);
        if(ticks==320)level.setBlockAndUpdate(crowd.getFirst(),Blocks.AIR.defaultBlockState());
    }
    @SubscribeEvent public static void client(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(!started){started=true;oldPause=mc.options.pauseOnLostFocus;mc.options.pauseOnLostFocus=false;build();return;}
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(!ready)return;
        try{
            if(ticks>=65&&checked.add(65)){
                for(var voice:MachineFeedback.Voice.values())check(mc.getSoundManager().getSoundEvent(voice.event().getLocation())!=null,"Registered audio resource "+voice);
                check(MachineAudio.profiles().contains("FURNACE")&&MachineAudio.profiles().contains("METAL_DRILL"),"Actual smelting and drilling start both work loops");
                check(MachineAudio.playingLoops()>=2,"Minecraft sound engine is playing the furnace and drill");
                Files.createDirectories(OUT);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve("directional-hatches.png"));}
            }
            if(ticks>=125&&checked.add(125))check(MachineAudio.activeLoops()==8,"Twelve nearby work sources respect the eight-loop cap");
            if(ticks>=205&&checked.add(205))check(MachineAudio.activeLoops()==2,"Expired synthetic work fades out while real machines continue");
            if(ticks>=280&&checked.add(280)){check(MachineAudio.activeLoops()==0,"Blocked furnace and drill outputs stop both loops");check(drill.speed()==0&&furnace.status().equals("Output blocked"),"Native machine state confirms output blocking");}
            if(ticks>=315&&checked.add(315))check(MachineAudio.activeLoops()==1&&MachineAudio.playingLoops()==1,"A fresh work lease restarts playback");
            if(ticks>=340){check(MachineAudio.activeLoops()==0,"Removing a sounding block leaves no orphan loop");done=true;mc.options.pauseOnLostFocus=oldPause;
                Files.writeString(OUT.resolve("results.txt"),String.join("\n",evidence)+"\n");mc.stop();}
        }catch(Exception ex){done=true;mc.options.pauseOnLostFocus=oldPause;try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("FAILED.txt"),ex.toString()+"\n"+String.join("\n",evidence));}catch(Exception ignored){}throw new RuntimeException(ex);}
    }
}
