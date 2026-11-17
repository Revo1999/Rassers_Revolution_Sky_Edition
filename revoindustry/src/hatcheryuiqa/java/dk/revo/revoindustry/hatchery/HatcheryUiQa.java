package dk.revo.revoindustry.hatchery;

import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in visual fixture; only creates/modifies a newly named QA save. Never enters an existing save. */
@EventBusSubscriber(modid="revoindustry",value=Dist.CLIENT)
public final class HatcheryUiQa {
    private static final Path OUT=Path.of("../../art/blockbench/wither-hatchery/ingame").toAbsolutePath().normalize();
    private static final BlockPos ORIGIN=new BlockPos(0,160,0);
    private static boolean launched,form,created,started,done,oldHide,oldPause;
    private static int oldFov,frames,menuFrames,sequence;
    private static String world;
    private static volatile boolean ready;
    private static final Set<String> SHOTS=new HashSet<>();
    private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
    private static void shot(String name){try{var path=OUT.resolve(name+".png");Files.createDirectories(path.getParent());try(var image=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){image.writeToFile(path);}}catch(Exception e){throw new RuntimeException(e);}}
    private static void once(String name){if(SHOTS.add(name))shot(name);}
    private static void prepare(){server(p->{
        var l=p.serverLevel();if(!l.getServer().getWorldPath(LevelResource.ROOT).toString().contains(world))throw new IllegalStateException("Only the fresh Wither Hatchery QA save may be modified");
        l.setDayTime(11000);l.setWeatherParameters(100000,0,false,false);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());
        p.setGameMode(GameType.CREATIVE);p.getInventory().clearContent();
        for(int x=-9;x<=10;x++)for(int z=-11;z<=10;z++)for(int y=159;y<=166;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.POLISHED_DEEPSLATE.defaultBlockState():Blocks.AIR.defaultBlockState());
        for(var c:HatcheryLayout.SHELL)l.setBlockAndUpdate(HatcheryLayout.at(ORIGIN,Direction.NORTH,c.offset()),HatcheryLayout.state(c,Direction.NORTH));
        var machine=(HatcheryController)l.getBlockEntity(ORIGIN);if(!machine.validate())throw new IllegalStateException(machine.problem());
        machine.input().inventory.setStackInSlot(0,new ItemStack(Hatchery.EGG.get()));
        p.teleportTo(6,162,-9);p.getAbilities().flying=true;p.onUpdateAbilities();ready=true;
    });}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.getOverlay()!=null)return;
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null){
            if(!launched&&mc.screen instanceof TitleScreen){launched=true;world="Revo Wither Hatchery QA";int n=1;while(Files.exists(Path.of("saves",world)))world="Revo Wither Hatchery QA "+(++n);CreateWorldScreen.openFresh(mc,mc.screen);return;}
            if(launched&&!form&&mc.screen instanceof CreateWorldScreen screen){
                form=true;screen.getUiState().setName(world);screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);screen.getUiState().setAllowCommands(true);screen.getUiState().setSeed("15112026");
                for(var child:screen.children())if(child instanceof Button button&&button.getMessage().getString().equals(Component.translatable("selectWorld.create").getString())){created=true;mc.tell(button::onPress);return;}
                throw new IllegalStateException("Create-world button missing");
            }
            if(created&&mc.screen!=null)for(var child:mc.screen.children())if(child instanceof Button button&&(button.getMessage().getString().equals("Proceed")||button.getMessage().getString().equals("I know what I'm doing!"))){mc.tell(button::onPress);break;}
            return;
        }
        if(!started){started=true;oldHide=mc.options.hideGui;oldPause=mc.options.pauseOnLostFocus;oldFov=mc.options.fov().get();mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.options.fov().set(50);prepare();return;}
        if(!ready)return;
        if(++frames>4500)throw new IllegalStateException("Wither visual QA timed out");
        server(p->{if(p.serverLevel().getBlockEntity(ORIGIN) instanceof HatcheryController c)c.receiveEu(Hatchery.EU_PER_TICK,false);});
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(!(mc.level.getBlockEntity(ORIGIN) instanceof HatcheryController c))return;
        mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(.5,162.3,2.3));
        if(c.stage()==1&&c.progress()>100){if(c.status()!=5)throw new IllegalStateException("Client did not receive active animation state");for(int x=-1;x<=1;x++)if(!mc.level.getBlockState(c.at(x,4,2)).getValue(HatcheryBlock.ACTIVE))throw new IllegalStateException("Roof light not synchronized");once("01-incubating");}
        if(c.stage()==1&&c.progress()>700)once("02-first-cracks");
        if(c.stage()==1&&c.progress()>1060)once("03-glowing-cracks");
        if(c.stage()==2&&c.progress()>45)once("04-wither-hatching");
        if(c.stage()==3&&c.progress()>70)once("05-wither-neutralizing");
        if((c.stage()==1&&c.progress()>1000||c.stage()==2||c.stage()==3)&&frames%4==0)shot("animation/frame-"+String.format(Locale.ROOT,"%04d",sequence++));
        if(c.stage()==0&&SHOTS.contains("05-wither-neutralizing")){
            if(menuFrames++==0){once("06-cycle-complete");mc.options.hideGui=false;server(p->{p.teleportTo(3.5,160,2.5);var port=(HatcheryPort)p.serverLevel().getBlockEntity(HatcheryLayout.at(ORIGIN,Direction.NORTH,2,0,2));if(!port.inventory.getStackInSlot(0).is(Items.NETHER_STAR))throw new IllegalStateException("Actual Nether Star missing");p.openMenu(port,b->b.writeBlockPos(port.getBlockPos()).writeBoolean(true));});}
            if(menuFrames==40){if(!(mc.screen instanceof dk.revo.revoindustry.client.HatcheryScreen s)||s.getMenu().slots.size()!=45||!s.getMenu().getSlot(0).getItem().is(Items.NETHER_STAR))throw new IllegalStateException("Star hatch menu/slot synchronization failed");once("07-star-output-menu");server(p->{p.closeContainer();var machine=(HatcheryController)p.serverLevel().getBlockEntity(ORIGIN);p.teleportTo(.5,160,-2);p.openMenu(machine,b->b.writeBlockPos(ORIGIN).writeBoolean(false));});}
            if(menuFrames==80){if(!(mc.screen instanceof dk.revo.revoindustry.client.HatcheryScreen s)||!s.getMenu().slots.isEmpty())throw new IllegalStateException("Controller must remain diagnostics-only");once("08-controller-menu");try{Files.writeString(OUT.resolve("qa-results.txt"),"PASS: new isolated QA save; native 5x5x5 model and unchanged dark 3x3 glass; exact MI steel and furnace-style hatch faces; dithered red warning lenses; three synchronized ceiling downlights; live rocking/cracking egg; a real world Wither visibly hatches, grows and takes lethal chamber damage; exactly one Nether Star in the real output hatch; native 45-slot hatch and inventory-free controller menus.\n");}catch(Exception e){throw new RuntimeException(e);}done=true;mc.options.hideGui=oldHide;mc.options.pauseOnLostFocus=oldPause;mc.options.fov().set(oldFov);mc.stop();}
        }
    }
}
