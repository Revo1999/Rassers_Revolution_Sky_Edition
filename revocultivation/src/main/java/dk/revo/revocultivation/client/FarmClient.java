package dk.revo.revocultivation.client;
import dk.revo.revocultivation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=RevoCultivation.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FarmClient {
    private static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"block/"+name));}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoCultivation.CONTROLLER_MENU.get(),FarmControllerScreen::new);e.register(RevoCultivation.HATCH_MENU.get(),FarmHatchScreen::new);}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(var kind:MachineKind.values())if(kind.registered())e.register(model(kind.id+"_body"));e.register(model("tree_grower_tree"));e.register(model("iron_greenhouse_lights"));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(RevoCultivation.MACHINE.get(),Renderer::new);}
    public static class Renderer implements BlockEntityRenderer<FarmMachine> {
        public Renderer(BlockEntityRendererProvider.Context context){}
        private void draw(String name,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,mc.getModelManager().getModel(model(name)),1,1,1,light,overlay);}
        @Override public void render(FarmMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            if(!machine.formed())return;var mc=Minecraft.getInstance();pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-machine.facing().toYRot()));pose.translate(-machine.kind().width()/2d,0,-.5);pose.scale(machine.kind().width(),machine.kind().width(),machine.kind().width());draw(machine.kind().id+"_body",pose,buffers,light,overlay);
            if(machine.kind().electric()&&machine.state()==1)draw("iron_greenhouse_lights",pose,buffers,LightTexture.FULL_BRIGHT,overlay);
            if(machine.kind().attachedTank()){
                float[] low=machine.kind().realTree()?new float[]{2.2f,3,35.2f}:new float[]{2.2f,3,35.2f};
                float[] high=machine.kind().realTree()?new float[]{12.8f,23,44.8f}:new float[]{10.8f,22,44.8f};
                dk.revo.revolib.client.FarmFluidRenderer.water(machine.getLevel(),machine.at(-machine.kind().radius(),0,0),machine.water.getFluidAmount(),machine.kind().waterCapacity(),pose,buffers,light,overlay,low,high,machine.kind().width()*16);
            }
            if(machine.program()>=0){
                float progress=Math.clamp((machine.progress()+(machine.state()==1?partial:0))/machine.kind().ticks,0,1);
                if(machine.kind().realTree()){
                    if(!machine.getLevel().getBlockState(machine.at(0,1,0)).is(Blocks.OAK_LOG)){
                        pose.pushPose();pose.translate(32f/80,16f/80,32f/80);pose.scale(1f/5,1f/5,1f/5);mc.getBlockRenderer().renderSingleBlock(Blocks.OAK_SAPLING.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                    }
                }
                else if(machine.kind().isTree()){pose.pushPose();pose.translate(.5,14f/48,.5);float scale=.35f+.65f*progress;pose.scale(scale,scale,scale);pose.translate(-.5,-14f/48,-.5);draw("tree_grower_tree",pose,buffers,light,overlay);pose.popPose();}
                else if(machine.kind().isGreenhouse()){
                    var crop=switch(machine.program()){case 11->Blocks.CARROTS.defaultBlockState().setValue(CarrotBlock.AGE,Math.min(7,(int)(progress*8)));case 12->Blocks.POTATOES.defaultBlockState().setValue(PotatoBlock.AGE,Math.min(7,(int)(progress*8)));case 13->Blocks.BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE,Math.min(3,(int)(progress*4)));default->Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,Math.min(7,(int)(progress*8)));};
                    if(machine.kind().electric()){
                        for(float x:new float[]{16,32,48})for(float z:new float[]{16,32,48}){pose.pushPose();pose.translate(x/80,16f/80,z/80);pose.scale(.2f,.2f,.2f);mc.getBlockRenderer().renderSingleBlock(crop,pose,buffers,light,overlay);pose.popPose();}
                    }else{pose.pushPose();pose.translate(16f/48,14f/48,16f/48);pose.scale(1f/3,1f/3,1f/3);mc.getBlockRenderer().renderSingleBlock(crop,pose,buffers,light,overlay);pose.popPose();}
                }else{
                    var mob=machine.containedMob();
                    if(mob!=null){
                        pose.pushPose();pose.translate(.5,5f/48,.5);pose.scale(1f/3,1f/3,1f/3);
                        // The vanilla renderer supplies species-specific geometry, textures,
                        // equipment, eye layers and idle/limb animation at actual mob scale.
                        // Invoke it directly to avoid world shadow/hitbox/fire passes for a
                        // machine-owned mob that is not in the world's entity collection.
                        mc.getEntityRenderDispatcher().getRenderer(mob).render(mob,180,partial,pose,buffers,light);
                        pose.popPose();
                    }
                }
            }
            pose.popPose();
        }
        @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(FarmMachine m){return new net.minecraft.world.phys.AABB(m.getBlockPos()).inflate(m.kind().realTree()?7:4);}
        @Override public boolean shouldRenderOffScreen(FarmMachine m){return true;}
    }
}
