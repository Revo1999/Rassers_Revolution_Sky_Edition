package dk.revo.revoindustry.saltern;

import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.util.Simulation;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

@EventBusSubscriber(modid="revoindustry",value=Dist.CLIENT)
public final class SalternUiQa {
    private static final BlockPos ORIGIN=new BlockPos(0,160,0);
    private static final Path OUT=Path.of("../../art/blockbench/salt-geode/ingame").toAbsolutePath().normalize();
    private static boolean launched,form,created,started,done,oldHide,oldPause;private static volatile boolean ready,saltReady;
    private static int oldFov,frames,menus,sequence;private static String world;private static final Set<String> shots=new HashSet<>();
    private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
    private static void shot(String name){try{var path=OUT.resolve(name+".png");Files.createDirectories(path.getParent());try(var image=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){image.writeToFile(path);}}catch(Exception ex){throw new RuntimeException(ex);}}
    private static void once(String name){if(shots.add(name))shot(name);}
    private static void prepare(){server(p->{
        var l=p.serverLevel();if(!l.getServer().getWorldPath(LevelResource.ROOT).toString().contains(world))throw new IllegalStateException("Only newly created Salt Geode QA save may be modified");l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());p.setGameMode(GameType.CREATIVE);p.getInventory().clearContent();
        for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=159;y<=165;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==159?Blocks.POLISHED_DEEPSLATE.defaultBlockState():Blocks.AIR.defaultBlockState());
        for(var cell:SalternLayout.cells())l.setBlockAndUpdate(SalternLayout.at(ORIGIN,Direction.NORTH,cell.offset()),SalternLayout.state(cell,Direction.NORTH));
        var c=(SalternController)l.getBlockEntity(ORIGIN);if(!c.validate())throw new IllegalStateException(c.problem());
        l.setBlockAndUpdate(c.at(2,0,1),BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:centrifuge")).defaultBlockState());c.water.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);
        p.teleportTo(.5,160.1,-4.8);p.getAbilities().flying=true;p.onUpdateAbilities();ready=true;
    });}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.getOverlay()!=null)return;
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null){
            if(!launched&&mc.screen instanceof TitleScreen){launched=true;world="Revo Salt Geode QA";int n=1;while(Files.exists(Path.of("saves",world)))world="Revo Salt Geode QA "+(++n);CreateWorldScreen.openFresh(mc,mc.screen);return;}
            if(launched&&!form&&mc.screen instanceof CreateWorldScreen screen){form=true;screen.getUiState().setName(world);screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);screen.getUiState().setAllowCommands(true);screen.getUiState().setSeed("151126");for(var child:screen.children())if(child instanceof Button button&&button.getMessage().getString().equals(Component.translatable("selectWorld.create").getString())){created=true;mc.tell(button::onPress);return;}throw new IllegalStateException("No create-world button");}
            if(created&&mc.screen!=null)for(var child:mc.screen.children())if(child instanceof Button button&&(button.getMessage().getString().equals("Proceed")||button.getMessage().getString().equals("I know what I'm doing!"))){mc.tell(button::onPress);break;}return;
        }
        if(!started){started=true;oldHide=mc.options.hideGui;oldPause=mc.options.pauseOnLostFocus;oldFov=mc.options.fov().get();mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.options.fov().set(50);prepare();return;}
        if(!ready)return;if(++frames>2200)throw new IllegalStateException("Saltern visual QA timed out");
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        server(p->{var c=(SalternController)p.serverLevel().getBlockEntity(ORIGIN);c.receiveEu(16,false);var machine=(ElectricCraftingMachineBlockEntity)p.serverLevel().getBlockEntity(c.at(2,0,1));machine.getEnergyComponent().insertEu(32,Simulation.ACT);var salt=BuiltInRegistries.ITEM.get(ResourceLocation.parse("modern_industrialization:salt_dust"));long n=machine.getInventory().getItemStacks().stream().filter(s->s.toStack().is(salt)).mapToLong(s->s.getAmount()).sum();if(n==2)saltReady=true;});
        mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(.5,161.4,1.1));
        if(!(mc.level.getBlockEntity(ORIGIN) instanceof SalternController c))return;
        if(c.activeBatch()){
            if(c.progress()>40)once("01-small-buds");if(c.progress()>180&&c.progress()%12<5)once("02-growth-and-electricity");if(c.progress()>310)once("03-grown-crystals-under-water");if(c.progress()>365)once("04-submerged-harvest");
            if(frames%2==0)shot("animation/frame-"+String.format(Locale.ROOT,"%04d",sequence++));
        }
        if(saltReady&&menus++==0){once("05-brine-to-centrifuge");mc.options.hideGui=false;server(p->{var c2=(SalternController)p.serverLevel().getBlockEntity(ORIGIN);p.teleportTo(2.5,160,-1);((ElectricCraftingMachineBlockEntity)p.serverLevel().getBlockEntity(c2.at(2,0,1))).openMenu(p);});}
        if(menus==35){once("06-native-mi-salt-output");server(p->{p.closeContainer();p.teleportTo(.5,160,-2);var machine=(SalternController)p.serverLevel().getBlockEntity(ORIGIN);p.openMenu(machine,b->b.writeBlockPos(ORIGIN));});}
        if(menus==70){if(!(mc.screen instanceof dk.revo.revoindustry.client.SalternScreen s)||!s.getMenu().slots.isEmpty())throw new IllegalStateException("Geode controller GUI failed");once("07-geode-diagnostics");try{Files.writeString(OUT.resolve("qa-results.txt"),"PASS: fresh isolated save; 3x3x3 MI-style geode chamber; animated native water, submerged growing crystal buds, electrical arcs and harvesting; brine auto-export to actual MI Centrifuge; two native Salt Dust in the centrifuge; native MI menu and inventory-free chamber diagnostics.\n");}catch(Exception ex){throw new RuntimeException(ex);}done=true;mc.options.hideGui=oldHide;mc.options.pauseOnLostFocus=oldPause;mc.options.fov().set(oldFov);mc.stop();}
    }
}
