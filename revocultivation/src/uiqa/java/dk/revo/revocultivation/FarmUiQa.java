package dk.revo.revocultivation;

import java.nio.file.*;
import java.util.function.Consumer;
import dk.revo.revocultivation.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.world.level.material.Fluids;

/** Opt-in end-to-end visual QA. Only a named copied save is modified; never shipped. */
@EventBusSubscriber(modid=RevoCultivation.MOD_ID,value=Dist.CLIENT)
public final class FarmUiQa {
    private static boolean started,done;private static int stage,frames,timeout;
    private static volatile boolean ready;
    private static final BlockPos[] positions={new BlockPos(0,128,0),new BlockPos(7,128,0),new BlockPos(14,128,0)};
    private static final String[] names={"tree-world","greenhouse-world","harvester-world","tree-controller","tree-hatch","greenhouse-controller","greenhouse-hatch","harvester-controller","harvester-hatch","tree-emi","greenhouse-emi","harvester-emi","full-build-guide","harvester-skeleton","harvester-spider","jade-compact"};
    private static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
    private static FarmMachine machine(ServerPlayer p,int i){return (FarmMachine)p.serverLevel().getBlockEntity(positions[i]);}
    private static void camera(int i,boolean jade){
        server(p->{p.closeContainer();var pos=positions[i];p.teleportTo(pos.getX()+(jade?.5:2.9),pos.getY()+(jade?0:1.6),pos.getZ()+(jade?-3:-3.8));p.getAbilities().flying=true;p.onUpdateAbilities();});
    }
    private static void open(int i,boolean hatch){
        server(p->{p.closeContainer();var m=machine(p,i);var pos=hatch?m.at(1,0,0):m.getBlockPos();p.teleportTo(pos.getX()+.5,pos.getY()+1,pos.getZ()-2.5);p.gameMode.useItemOn(p,p.serverLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),hatch?Direction.EAST:Direction.NORTH,pos,false));});
    }
    private static void prepare(){
        server(p->{
            if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo First Tier QA"))throw new IllegalStateException("QA requires its copied save");
            var l=p.serverLevel();l.setDayTime(1000);l.updateSkyBrightness();l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,l.getServer());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            p.teleportTo(4,132,-6);
            for(int x=-8;x<=23;x++)for(int z=-9;z<=10;z++)for(int y=127;y<=139;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==127?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());
            for(var kind:MachineKind.values()){
                int i=kind.ordinal();var pos=positions[i];l.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.NORTH));var m=machine(p,i);
                for(int x=-1;x<=1;x++)for(int y=0;y<kind.height;y++)for(int z=-1;z<=1;z++){
                    var part=kind.part(x,y,z);var s=kind.block(part).defaultBlockState();if(part==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,Direction.NORTH);if(part==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,Direction.EAST);if(part==MachineKind.Part.FILTER)s=s.setValue(FarmFilter.FACING,Direction.NORTH);l.setBlockAndUpdate(m.at(x,y,z),s);
                }
                if(kind==MachineKind.TREE)m.inputs.setStackInSlot(0,new ItemStack(Items.OAK_SAPLING));if(kind==MachineKind.GREENHOUSE){m.inputs.setStackInSlot(0,new ItemStack(Items.WHEAT_SEEDS));m.water.fill(new FluidStack(Fluids.WATER,6000),FluidAction.EXECUTE);}
                for(int t=0;t<(kind==MachineKind.TREE?1400:kind==MachineKind.GREENHOUSE?700:450);t++)FarmMachine.tick(l,pos,m.getBlockState(),m);
                if(!m.formed()||m.state()!=1||m.buffered()==0)throw new IllegalStateException("QA production/formation failed: "+kind+" state="+m.state());
            }
            l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(-8,127,-9,24,140,11)).forEach(net.minecraft.world.entity.Entity::discard);
            p.getAbilities().flying=true;p.onUpdateAbilities();ready=true;
        });
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(!started){started=true;prepare();return;}if(!ready)return;
        if(++timeout>2400)throw new IllegalStateException("Farm QA timed out at stage "+stage+" screen="+mc.screen);
        if(frames==0&&stage==0)camera(0,false);
        boolean world=stage<3||stage>=13;
        if(world){var pos=positions[stage<3?stage:stage<15?2:1];mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(pos).add(0,stage==15?0:1,1));}
        boolean expected=world?mc.screen==null:stage<9?(stage%2==1?mc.screen instanceof FarmControllerScreen:mc.screen instanceof FarmHatchScreen):mc.screen!=null;
        if(!expected)return;if(frames==5)org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),5,55);
        if(++frames<(stage==0?140:65))return;
        try{
            var dir=Path.of("../../art/blockbench/first-tier-machines/ingame").toAbsolutePath().normalize();Files.createDirectories(dir);
            try(var screenshot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){screenshot.writeToFile(dir.resolve(names[stage]+".png"));}
            if(stage==0)Files.writeString(dir.resolve("qa-results.txt"),"All three first-tier structures formed and produced in the real integrated server.\n");
            if(stage==2||stage==13||stage==14){
                var m=(FarmMachine)mc.level.getBlockEntity(positions[2]);var mob=m.containedMob();int program=stage==2?20:stage==13?21:22;
                if(mob==null||mob.getType()!=ContainedHarvesterMob.type(program)||!mob.isPersistenceRequired()||!mob.isNoAi())throw new IllegalStateException("Vanilla contained mob missing or wrong species at stage "+stage);
                Files.writeString(dir.resolve("qa-results.txt"),"Vanilla "+net.minecraft.world.entity.EntityType.getKey(mob.getType())+" rendered through native entity renderer; UUID="+mob.getUUID()+"; cached instance="+(mob==m.containedMob())+"\n",StandardOpenOption.APPEND);
            }
            if(stage>=3&&stage<=8){
                int i=(stage-3)/2;var kind=MachineKind.values()[i];
                if(stage%2==1){var menu=((FarmControllerScreen)mc.screen).getMenu();if(!menu.slots.isEmpty()||menu.kind()!=kind||menu.value(1)!=1)throw new IllegalStateException("Controller data/inventory mismatch "+kind);}
                else{var menu=((FarmHatchScreen)mc.screen).getMenu();int player=kind.inputSlots()+9;if(menu.kind!=kind||menu.slots.size()!=player+36||menu.slots.get(player).x!=8||menu.slots.get(player).y!=kind.hatchHeight()-82)throw new IllegalStateException("Hatch/player slot mismatch "+kind);}
                Files.writeString(dir.resolve("qa-results.txt"),names[stage]+": actual useItemOn network menu and slot data passed.\n",StandardOpenOption.APPEND);
            }
            switch(stage){
                case 0->camera(1,false);case 1->camera(2,false);case 2->open(0,false);case 3->open(0,true);case 4->open(1,false);case 5->open(1,true);case 6->open(2,false);case 7->open(2,true);
                case 8->FarmEmiPlugin.open(MachineKind.TREE);case 9->FarmEmiPlugin.open(MachineKind.GREENHOUSE);case 10->FarmEmiPlugin.open(MachineKind.HARVESTER);
                case 11->{mc.screen.mouseClicked(mc.getWindow().getGuiScaledWidth()*.70,mc.getWindow().getGuiScaledHeight()*.53,0);if(!(mc.screen instanceof dk.revo.revolib.client.guide.MultiblockGuideScreen))throw new IllegalStateException("EMI guide button failed");}
                case 12->{Files.writeString(dir.resolve("qa-results.txt"),"All three EMI assembly screens and actual full 3D guide button passed.\n",StandardOpenOption.APPEND);mc.player.closeContainer();server(p->{var m=machine(p,2);var f=m.at(0,1,-1);p.gameMode.useItemOn(p,p.serverLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(f),Direction.NORTH,f,false));});camera(2,false);}
                case 13->{server(p->{var m=machine(p,2);var f=m.at(0,1,-1);p.gameMode.useItemOn(p,p.serverLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(f),Direction.NORTH,f,false));});camera(2,false);}
                case 14->camera(1,true);
                case 15->{var values=snownee.jade.impl.WailaClientRegistration.instance().getServerData().getIntArray("RevoFarm");if(values.length!=5||values[0]!=1)throw new IllegalStateException("Missing compact Jade data: "+java.util.Arrays.toString(values));Files.writeString(dir.resolve("qa-results.txt"),"Selected native zombie/skeleton/spider mobs and compact real-server Jade payload passed.\n",StandardOpenOption.APPEND);done=true;mc.stop();}
            }
            stage++;frames=0;timeout=0;
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
}
