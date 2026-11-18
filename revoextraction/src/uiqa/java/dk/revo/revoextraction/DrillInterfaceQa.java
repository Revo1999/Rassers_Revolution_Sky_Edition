package dk.revo.revoextraction;

import java.nio.file.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crank.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in visual QA, running only in a copied test save. Not shipped in production. */
@EventBusSubscriber(modid=RevoExtraction.MOD_ID,value=Dist.CLIENT)
public final class DrillInterfaceQa {
    private static boolean started,done;
    private static int frames,stage;
    private static volatile BlockPos hatchPos;
    private static volatile BlockPos crankPos;
    private static volatile BlockPos controllerPos;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(done||mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
        if(!started){
            started=true;
            var server=mc.getSingleplayerServer();
            server.execute(()->{
                if(!server.getWorldData().getLevelName().equals("Revo Drill Interface QA") && !mc.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toString().contains("Revo Drill Interface QA"))return;
                var player=server.getPlayerList().getPlayer(mc.player.getUUID());
                var level=player.serverLevel();
                BlockPos pos=player.blockPosition().offset(0,1,-3);
                controllerPos=pos;
                level.setBlockAndUpdate(pos,RevoExtraction.CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,Direction.SOUTH));
                var machine=(DrillMachine)level.getBlockEntity(pos);
                for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++) {
                    var state=switch(DrillLayout.part(x,y,z)) {
                        case LOG -> RevoExtraction.SUPPORT.get().defaultBlockState();
                        case CASING -> RevoExtraction.CASING.get().defaultBlockState();
                        case CORE -> RevoExtraction.CORE.get().defaultBlockState();
                        case CONTROLLER -> machine.getBlockState();
                        case INPUT -> RevoExtraction.INPUT.get().defaultBlockState().setValue(DrillInput.FACING,machine.facing().getClockWise());
                        case HATCH -> RevoExtraction.HATCH.get().defaultBlockState().setValue(DrillHatch.FACING,machine.facing().getCounterClockWise());
                        case FLUID_HATCH -> RevoExtraction.STEEL_FLUID_HATCH.get().defaultBlockState();
                case AIR -> Blocks.AIR.defaultBlockState();
                    };
                    level.setBlockAndUpdate(machine.at(x,y,z),state);
                }
                machine.checkStructure();
                machine.inventory.setStackInSlot(0,new ItemStack(Items.COBBLESTONE,12));
                machine.inventory.setStackInSlot(1,new ItemStack(Items.GRAVEL,8));
                machine.inventory.setStackInSlot(2,new ItemStack(Items.RAW_IRON,2));
                hatchPos=machine.at(-1,0,0);
                crankPos=machine.at(1,1,0).relative(machine.facing().getClockWise());
                level.setBlockAndUpdate(crankPos,AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,machine.facing().getClockWise()));
                player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),machine.facing(),pos,false));
            });
        }
        if(crankPos!=null){var pos=crankPos;mc.getSingleplayerServer().execute(()->{var level=mc.getSingleplayerServer().overworld();if(level.getBlockEntity(pos) instanceof HandCrankBlockEntity crank)crank.turn(false);});}
        if(stage>=7)mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(stage==9?hatchPos:controllerPos));
        boolean expected=stage==0||stage==6?mc.screen instanceof DrillScreen:stage==1?mc.screen instanceof DrillHatchScreen:stage>=7?mc.screen==null:mc.screen!=null;
        if(!expected)return;
        if(frames==5)org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),5,55);
        if(++frames<(stage==0?160:60))return;
        try {
            Path dir=Path.of("../../art/blockbench/revoextraction").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String file=switch(stage){case 0->"controller-interface-ingame.png";case 1->"hatch-interface-ingame.png";case 2->"emi-multiblock-ingame.png";case 3->"multiblock-guide-ingame.png";case 4->"multiblock-guide-foundation-ingame.png";case 5->"multiblock-guide-ports-ingame.png";case 6->"controller-orientation-error-ingame.png";case 7->"jade-orientation-error-ingame.png";case 8->"jade-working-ingame.png";default->"jade-hatch-ingame.png";};
            try(var screenshot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){screenshot.writeToFile(dir.resolve(file));}
            if(stage==0){
                var menu=((DrillScreen)mc.screen).getMenu();
                if(!menu.slots.isEmpty())throw new IllegalStateException("Controller still exposes inventory");
                Files.copy(dir.resolve(file),dir.resolve("drill-interface-ingame.png"),StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Controller opened through useItemOn; slots="+menu.slots.size()+"; rpm="+menu.rpm()+"; state="+menu.state()+"; progress="+menu.progress()+"\n");
                mc.getSingleplayerServer().execute(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayer(mc.player.getUUID());var level=player.serverLevel();
                    player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(hatchPos),level.getBlockState(hatchPos).getValue(DrillHatch.FACING),hatchPos,false));
                });
            }else if(stage==1){
                var menu=((DrillHatchScreen)mc.screen).getMenu();
                if(menu.slots.size()!=45)throw new IllegalStateException("Wrong hatch inventory layout");
                if(menu.slots.get(9).x!=8||menu.slots.get(9).y!=102||menu.slots.get(36).y!=160)throw new IllegalStateException("Nonstandard player inventory spacing");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Hatch opened through useItemOn; slots="+menu.slots.size()+"; output slots=9; player slots=36\n",StandardOpenOption.APPEND);
                dev.emi.emi.api.EmiApi.displayRecipe(new DrillEmiPlugin.Assembly());
            }else if(stage==2){
                var guide=DrillEmiPlugin.guide();
                if(guide.blocks()!=26||guide.layers()!=3||guide.materials().stream().mapToInt(ItemStack::getCount).sum()!=26)throw new IllegalStateException("Guide material/blueprint mismatch");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"EMI 3D recipe screen="+mc.screen.getClass().getSimpleName()+"; three layers; 26 required blocks including controller\n",StandardOpenOption.APPEND);
                mc.screen.mouseClicked(mc.getWindow().getGuiScaledWidth()*.70,mc.getWindow().getGuiScaledHeight()*.53,0);
                if(!(mc.screen instanceof dk.revo.revolib.client.guide.MultiblockGuideScreen))throw new IllegalStateException("Open 3D guide button did not open guide");
            }else if(stage==3){
                var guide=(dk.revo.revolib.client.guide.MultiblockGuideScreen)mc.screen;
                int w=Math.min(guide.width-16,560),h=Math.min(guide.height-16,340),x=(guide.width-w)/2,y=(guide.height-h)/2;
                guide.mouseClicked(x+20,y+h-60,0);guide.mouseClicked(x+20,y+h-60,0);guide.mouseClicked(x+90,y+h-60,0);
                if(guide.preview.layer()!=0||!guide.preview.single())throw new IllegalStateException("Layer buttons failed");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Full-size guide rendered; selected foundation single-layer view\n",StandardOpenOption.APPEND);
            }else if(stage==4){
                var guide=(dk.revo.revolib.client.guide.MultiblockGuideScreen)mc.screen;
                int w=Math.min(guide.width-16,560),h=Math.min(guide.height-16,340),x=(guide.width-w)/2,y=(guide.height-h)/2;
                guide.mouseClicked(x+44,y+h-60,0);
                guide.mouseClicked(x+(int)(w*.55)+30,y+141,0);
                guide.preview.rotate(-70);guide.preview.zoom(1);
                if(guide.preview.layer()!=1||!guide.preview.highlightLabel().equals("All ports"))throw new IllegalStateException("Port/layer buttons failed");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Layer controls, single-layer toggle, port highlighting, rotation and zoom exercised\n",StandardOpenOption.APPEND);
            }else if(stage==5){
                ((dk.revo.revolib.client.guide.MultiblockGuideScreen)mc.screen).onClose();
                if(!mc.screen.getClass().getSimpleName().equals("RecipeScreen"))throw new IllegalStateException("Back did not restore EMI");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Guide views captured; actual open/layer/mode/port buttons and Back navigation passed\n",StandardOpenOption.APPEND);
                mc.getSingleplayerServer().execute(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayer(mc.player.getUUID());var level=player.serverLevel();
                    level.setBlockAndUpdate(hatchPos,level.getBlockState(hatchPos).setValue(DrillHatch.FACING,Direction.WEST));
                    ((DrillMachine)level.getBlockEntity(controllerPos)).checkStructure();
                    player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(controllerPos),Direction.SOUTH,controllerPos,false));
                });
            }else if(stage==6){
                var menu=((DrillScreen)mc.screen).getMenu();
                if(menu.state()!=5||menu.snapshot().part()!=DrillLayout.Part.HATCH.ordinal()+1)throw new IllegalStateException("Facing issue not synchronized to controller menu");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Wrong hatch orientation synchronized to controller UI with part, offset and required direction\n",StandardOpenOption.APPEND);
                mc.player.closeContainer();
                mc.getSingleplayerServer().execute(()->{
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var level=player.serverLevel();
                    for(int x=-1;x<=1;x++)for(int z=1;z<=5;z++){
                        for(int y=-1;y<=2;y++)level.setBlockAndUpdate(controllerPos.offset(x,y,z),Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(controllerPos.offset(x,-2,z),Blocks.STONE.defaultBlockState());
                    }
                    player.teleportTo(controllerPos.getX()+.5,controllerPos.getY()-1,controllerPos.getZ()+3.5);
                });
            }else if(stage==7||stage==8){
                var data=snownee.jade.impl.WailaClientRegistration.instance().getServerData();var values=data.getIntArray("RevoDrill");
                if(values.length!=3||values[2]!=(stage==7?0:1))throw new IllegalStateException("Jade compact diagnostic payload incorrect, stage="+stage+" data="+data);
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Jade compact real server-data HUD passed: "+(stage==7?"generic incomplete status; no orientation fields":"working RPM and progress")+"\n",StandardOpenOption.APPEND);
                if(stage==7)mc.getSingleplayerServer().execute(()->{
                    var level=mc.getSingleplayerServer().overworld();level.setBlockAndUpdate(hatchPos,level.getBlockState(hatchPos).setValue(DrillHatch.FACING,Direction.EAST));
                    ((DrillMachine)level.getBlockEntity(controllerPos)).checkStructure();
                });
                else mc.getSingleplayerServer().execute(()->{
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var level=player.serverLevel();
                    for(int x=1;x<=5;x++)for(int z=-1;z<=1;z++){
                        for(int y=0;y<=2;y++)level.setBlockAndUpdate(hatchPos.offset(x,y,z),Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(hatchPos.offset(x,-1,z),Blocks.STONE.defaultBlockState());
                    }
                    player.teleportTo(hatchPos.getX()+3.5,hatchPos.getY(),hatchPos.getZ()+.5);
                });
            }else{
                if(!snownee.jade.impl.WailaClientRegistration.instance().getServerData().contains("RevoOutputCount"))throw new IllegalStateException("Jade hatch output data missing");
                Files.writeString(dir.resolve("drill-interface-qa.txt"),"Jade hatch HUD captured with output buffer and auto-output hint\n",StandardOpenOption.APPEND);
                done=true;mc.stop();
            }
            stage++;frames=0;
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
