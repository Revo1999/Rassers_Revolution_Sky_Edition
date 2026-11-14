package dk.revo.revoextraction;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crank.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Fixture runs only in the specifically named COPY of the gallery save. */
@EventBusSubscriber(modid="revoextraction",value=Dist.CLIENT)
public final class SluiceVisualQa {
    static final BlockPos ORIGIN=new BlockPos(100,180,100);
    static boolean started,done;static volatile boolean ready;static int ticks,frame,stage;
    static HandCrankBlockEntity drive,pumpDrive;static SluiceEntity machine;
    static Path dir(){return Path.of("../../screenshots/sluice").toAbsolutePath().normalize();}
    static boolean allowed(net.minecraft.server.MinecraftServer server){return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toString().contains("Revo Sluice QA");}
    static void prepare(){
        var mc=Minecraft.getInstance();
        mc.getSingleplayerServer().execute(()->{
            try{
                var server=mc.getSingleplayerServer();if(!allowed(server))throw new IllegalStateException("Sluice QA copy required");
                var player=server.getPlayerList().getPlayer(mc.player.getUUID());var l=player.serverLevel();
                player.closeContainer();player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);
                l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,server);
                for(int x=-8;x<=8;x++)for(int y=-1;y<9;y++)for(int z=-8;z<=8;z++)l.setBlockAndUpdate(ORIGIN.offset(x,y,z),y==-1?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
                var state=SluiceContent.SLUICE.get().defaultBlockState();
                l.setBlockAndUpdate(ORIGIN,state);SluiceContent.SLUICE.get().setPlacedBy(l,ORIGIN,state,null,new ItemStack(SluiceContent.SLUICE.get()));
                l.setBlockAndUpdate(ORIGIN.above(),SluiceContent.FEEDER.get().defaultBlockState());
                machine=(SluiceEntity)l.getBlockEntity(ORIGIN);
                machine.feeder().input.setStackInSlot(0,new ItemStack(Items.GRAVEL,64));
                for(int i:new int[]{1,3})l.setBlockAndUpdate(ORIGIN.above().north(i),AllBlocks.FLUID_PIPE.getDefaultState());
                var pumpPos=ORIGIN.above().north(2);
                l.setBlockAndUpdate(pumpPos,AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(BlockStateProperties.FACING,Direction.SOUTH));
                l.setBlockAndUpdate(pumpPos.above(),AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.Z));
                l.setBlockAndUpdate(pumpPos.above().south(),AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,Direction.SOUTH));
                pumpDrive=(HandCrankBlockEntity)l.getBlockEntity(pumpPos.above().south());
                var source=ORIGIN.above().north(4);l.setBlockAndUpdate(source,AllBlocks.FLUID_TANK.getDefaultState());
                l.getCapability(Capabilities.FluidHandler.BLOCK,source,Direction.SOUTH).fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,8000),FluidAction.EXECUTE);
                l.setBlockAndUpdate(ORIGIN.west(),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
                l.setBlockAndUpdate(ORIGIN.west(2),AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,Direction.WEST));
                drive=(HandCrankBlockEntity)l.getBlockEntity(ORIGIN.west(2));
                // A real inventory connected to the far-end extraction port.
                l.setBlockAndUpdate(ORIGIN.south(2).east(),Blocks.CHEST.defaultBlockState());
                player.connection.teleport(95,184,106,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
                if(!machine.assembled())throw new IllegalStateException("Sluice did not assemble");
                l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(ORIGIN).inflate(9)).forEach(net.minecraft.world.entity.Entity::discard);
                System.out.println("SLUICE QA ready "+machine.getBlockPos()+" feeder="+machine.feeder());
                ready=true;
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
    }
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post event){
        if(!ready||done||!allowed(event.getServer()))return;
        drive.turn(false);pumpDrive.turn(false);
        if(event.getServer().getTickCount()%100==0)System.out.println("SLUICE QA server formed="+machine.assembled()+" feeder="+machine.feeder()+" cycles="+machine.cycle+" rpm="+machine.getSpeed());
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(!started){started=true;mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.options.fov().set(48);prepare();return;}
        if(!ready)return;
        if(ticks%100==0){var client=mc.level.getBlockEntity(ORIGIN);System.out.println("SLUICE QA client "+client+" formed="+(client instanceof SluiceEntity e&&e.assembled())+" flowing="+(client instanceof SluiceEntity e&&e.flowing));}
        if(stage==3){if(Files.exists(dir().resolve("result.txt"))){done=true;mc.options.hideGui=false;mc.stop();}return;}
        mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(100.5,181,99.75));
        if(++ticks<100)return;
        try{
            Files.createDirectories(dir());
            if(stage==0){
                // Record several crank angles and moving flow textures, not only a rest pose.
                if(ticks%2==0){
                    try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir().resolve(String.format("working-%02d.png",frame)));}
                    if(frame++==0)try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir().resolve("overview.png"));}
                }
                if(frame>=24){stage=1;ticks=0;mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).connection.teleport(95,183,99.75,0,0));}
            }else if(stage==1){
                try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir().resolve("shaft-and-pump.png"));}
                stage=2;ticks=0;mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).connection.teleport(104,184,105,0,0));
            }else{
                try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir().resolve("water-channel.png"));}
                stage=3;
                mc.getSingleplayerServer().execute(()->{
                    try{
                        if(!machine.assembled()||machine.feeder()==null||machine.cycle==0)throw new IllegalStateException("Visual fixture lost its working assembly at "+machine.getBlockPos());
                        Files.writeString(dir().resolve("result.txt"),"PASS: formed="+machine.assembled()+", water="+machine.feeder().water.getFluidAmount()+", cycles="+machine.cycle+", rpm="+machine.getSpeed()+"; actual Create tank, pump, pipes and crank networks.\n");
                    }catch(Exception ex){throw new RuntimeException(ex);}
                });
            }
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
}
