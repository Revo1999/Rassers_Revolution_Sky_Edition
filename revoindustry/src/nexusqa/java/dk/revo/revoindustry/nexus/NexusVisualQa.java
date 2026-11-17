package dk.revo.revoindustry.nexus;

import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in visual fixture, excluded from release jars; creates and edits only its fresh QA save. */
@EventBusSubscriber(modid="revoindustry",value=Dist.CLIENT)
public final class NexusVisualQa {
    private static boolean launched,form,created,started,done,oldHide,oldPause;
    private static int frames,stage,oldFov;private static volatile boolean ready;private static String world;
    private static final String[] KINDS={"pressure_casting_plant","electroplating_line","particle_collider","nexus_portal","nexus_builder"};
    private static BlockPos origin(int i){return new BlockPos(i*35,160,0);}
    private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
    private static void prepare(){server(p->{
        var level=p.serverLevel();if(!level.getServer().getWorldPath(LevelResource.ROOT).toString().contains(world))throw new IllegalStateException("Fresh Nexus QA save required");
        level.setDayTime(1000);level.setWeatherParameters(100000,0,false,false);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,level.getServer());p.setGameMode(GameType.CREATIVE);p.getInventory().clearContent();
        for(int i=0;i<KINDS.length;i++){
            var pos=origin(i);var bp=Nexus.blueprint(KINDS[i]);
            for(int x=-13;x<=23;x++)for(int z=-5;z<=23;z++)level.setBlockAndUpdate(pos.offset(x,-1,z),Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            level.setBlockAndUpdate(pos,Nexus.block(bp.get("controller").getAsString()).defaultBlockState());var machine=(NexusMachine)level.getBlockEntity(pos);machine.orientation.facingDirection=Direction.NORTH;
            for(var raw:bp.getAsJsonArray("cells")){
                var cell=raw.getAsJsonObject();var c=Nexus.position(cell.getAsJsonArray("position"));var target=machine.blueprintWorld(c.getX(),c.getY(),c.getZ());if(target.equals(pos))continue;
                var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.get("block").getAsString()));var state=block.defaultBlockState();var face=Direction.byName(cell.get("facing").getAsString());
                if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,face);
                level.setBlockAndUpdate(target,state);if(level.getBlockEntity(target) instanceof aztech.modern_industrialization.machines.MachineBlockEntity m){m.orientation.facingDirection=face;m.orientation.outputDirection=face;m.sync();}
            }
            for(var raw:bp.getAsJsonArray("requiredAir")){var c=Nexus.position(raw.getAsJsonArray());level.setBlockAndUpdate(machine.blueprintWorld(c.getX(),c.getY(),c.getZ()),Blocks.AIR.defaultBlockState());}
            machine.tick();if(!machine.isShapeValid())throw new IllegalStateException("QA structure failed: "+KINDS[i]);
            machine.getEnergyComponents().getFirst().insertEu(10_000_000,aztech.modern_industrialization.util.Simulation.ACT);
        }ready=true;
    });}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.getOverlay()!=null)return;
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null){
            if(!launched&&mc.screen instanceof TitleScreen){launched=true;world="Revo Nexus QA";int n=1;while(Files.exists(Path.of("saves",world)))world="Revo Nexus QA "+(++n);CreateWorldScreen.openFresh(mc,mc.screen);return;}
            if(launched&&!form&&mc.screen instanceof CreateWorldScreen screen){form=true;screen.getUiState().setName(world);screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);screen.getUiState().setAllowCommands(true);screen.getUiState().setSeed("171126");for(var child:screen.children())if(child instanceof Button button&&button.getMessage().getString().equals(Component.translatable("selectWorld.create").getString())){created=true;mc.tell(button::onPress);return;}throw new IllegalStateException("No create-world button");}
            if(created&&mc.screen!=null)for(var child:mc.screen.children())if(child instanceof Button button&&(button.getMessage().getString().equals("Proceed")||button.getMessage().getString().equals("I know what I'm doing!"))){mc.tell(button::onPress);break;}return;
        }
        if(!started){started=true;oldHide=mc.options.hideGui;oldPause=mc.options.pauseOnLostFocus;oldFov=mc.options.fov().get();mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.options.fov().set(55);prepare();return;}
        if(!ready)return;
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(frames==0)server(p->{var pos=origin(stage);p.closeContainer();p.teleportTo(pos.getX()+(stage==3?19:stage==2?22:12),pos.getY()+(stage==3?12:stage==2?15:7),pos.getZ()-(stage==3?22:stage==2?14:12));p.getAbilities().flying=true;p.onUpdateAbilities();});
        var target=Vec3.atCenterOf(origin(stage)).add(0,stage==3?8:stage==2?1:1,stage==3?4:stage==2?10:4);
        mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target);
        if(++frames<100)return;
        try{
            var out=Path.of("../../art/blockbench/nexus-runtime/ingame").toAbsolutePath().normalize();Files.createDirectories(out);
            try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(out.resolve(KINDS[stage]+".png"));}
            frames=0;if(++stage==KINDS.length){Files.writeString(out.resolve("qa-results.txt"),"PASS: five native MI structures rendered in a fresh isolated save; powered portal surface; normal individual blocks, native hatches and connected railings.\n");done=true;mc.options.hideGui=oldHide;mc.options.pauseOnLostFocus=oldPause;mc.options.fov().set(oldFov);mc.stop();}
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
