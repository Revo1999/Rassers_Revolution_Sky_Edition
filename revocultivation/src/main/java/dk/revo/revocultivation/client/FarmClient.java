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
    private static final java.util.Map<String,ModelResourceLocation> MODEL_IDS=new java.util.concurrent.ConcurrentHashMap<>();
    private static ModelResourceLocation model(String name){return MODEL_IDS.computeIfAbsent(name,n->ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"block/"+n)));}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoCultivation.CONTROLLER_MENU.get(),FarmControllerScreen::new);e.register(RevoCultivation.HATCH_MENU.get(),FarmHatchScreen::new);}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(var kind:MachineKind.values())if(kind.registered())e.register(model(kind.id+"_body"));e.register(model("salt_crystal_growth"));e.register(model("iron_greenhouse_lights"));e.register(model("iron_greenhouse_glass"));e.register(model("steel_tree_grower_glass"));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(RevoCultivation.MACHINE.get(),Renderer::new);}
    public static class Renderer implements BlockEntityRenderer<FarmMachine> {
        private static final float[] WATER_LOW={2.2f,3,35.2f},TREE_WATER_HIGH={12.8f,23,44.8f},GREENHOUSE_WATER_HIGH={10.8f,22,44.8f},CROP_GRID={16,32,48};
        public Renderer(BlockEntityRendererProvider.Context context){}
        private void draw(String name,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(name.endsWith("_glass")?RenderType.entityTranslucentCull(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS):RenderType.cutout()),null,mc.getModelManager().getModel(model(name)),1,1,1,light,overlay);}
        @Override public void render(FarmMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            if(!machine.formed())return;var mc=Minecraft.getInstance();pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-machine.facing().toYRot()));pose.translate(-machine.kind().width()/2d,0,-.5);pose.scale(machine.kind().width(),machine.kind().width(),machine.kind().width());draw(machine.kind().id+"_body",pose,buffers,light,overlay);
            if(machine.kind().attachedTank())draw(machine.kind().id+"_glass",pose,buffers,light,overlay);
            if(machine.growLightsOn())draw("iron_greenhouse_lights",pose,buffers,LightTexture.FULL_BRIGHT,overlay);
            if(machine.kind().attachedTank()){
                float[] low=WATER_LOW;
                float[] high=machine.kind().realTree()?TREE_WATER_HIGH:GREENHOUSE_WATER_HIGH;
                dk.revo.revolib.client.FarmFluidRenderer.water(machine.getLevel(),machine.at(-machine.kind().radius(),0,0),machine.water.getFluidAmount(),machine.kind().waterCapacity(),pose,buffers,light,overlay,low,high,machine.kind().width()*16);
            }
            if(machine.program()>=0){
                float progress=Math.clamp((machine.progress()+(machine.state()==1?partial:0))/machine.cycleTicks(),0,1);
                if(machine.kind().realTree()){
                    if(!machine.getLevel().getBlockState(machine.at(0,1,0)).is(Blocks.OAK_LOG)){
                        pose.pushPose();pose.translate(32f/80,16f/80,32f/80);pose.scale(1f/5,1f/5,1f/5);mc.getBlockRenderer().renderSingleBlock(Blocks.OAK_SAPLING.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                    }
                }
                else if(machine.kind().isTree()){
                    pose.pushPose();pose.translate(.5,14f/48,.5);
                    float blockScale=(.35f+.65f*progress)/8;
                    pose.scale(blockScale,blockScale,blockScale);
                    for(int[] block:MiniOakGeometry.BLOCKS){
                        pose.pushPose();pose.translate(block[0]-.5,block[1],block[2]-.5);
                        mc.getBlockRenderer().renderSingleBlock((block[3]==0?Blocks.OAK_LOG:Blocks.OAK_LEAVES).defaultBlockState(),pose,buffers,light,overlay);
                        pose.popPose();
                    }
                    pose.popPose();
                }
                else if(machine.kind().isGreenhouse()){
                    boolean submerged=machine.program()==15||machine.program()==19;
                    if(submerged&&machine.water.getFluidAmount()>0){
                        float size=machine.kind().width()*16;
                        float[] lo=machine.kind().electric()?new float[]{16.2f,16.1f,16.2f}:new float[]{16.2f,14.1f,16.2f};
                        float[] hi=machine.kind().electric()?new float[]{63.8f,31.7f,63.8f}:new float[]{31.8f,29.7f,31.8f};
                        dk.revo.revolib.client.FarmFluidRenderer.water(machine.getLevel(),machine.at(0,1,0),1,1,pose,buffers,light,overlay,lo,hi,size);
                    }
                    var crop=switch(machine.program()){case 11->Blocks.CARROTS.defaultBlockState().setValue(CarrotBlock.AGE,Math.min(7,(int)(progress*8)));case 12->Blocks.POTATOES.defaultBlockState().setValue(PotatoBlock.AGE,Math.min(7,(int)(progress*8)));case 32->RunningShoes.CROP.get().getStateForAge(Math.min(7,(int)(progress*8)));case 17->Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE,Math.min(3,(int)(progress*4)));case 18->Blocks.CHORUS_FLOWER.defaultBlockState();case 19->Blocks.AMETHYST_CLUSTER.defaultBlockState();case 31->Blocks.POPPY.defaultBlockState();case 14->Blocks.SUGAR_CANE.defaultBlockState();case 15->Blocks.KELP.defaultBlockState();case 16->Blocks.CACTUS.defaultBlockState();case 13->Blocks.BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE,Math.min(3,(int)(progress*4)));default->Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,Math.min(7,(int)(progress*8)));};
                    if(machine.kind().electric()){
                        for(float x:CROP_GRID)for(float z:CROP_GRID){pose.pushPose();pose.translate(x/80,16f/80,z/80);pose.scale(.2f,.2f,.2f);renderCrop(machine,crop,progress,x+z,partial,pose,buffers,light,overlay);pose.popPose();}
                    }else{pose.pushPose();pose.translate(16f/48,14f/48,16f/48);pose.scale(1f/3,1f/3,1f/3);renderCrop(machine,crop,progress,0,partial,pose,buffers,light,overlay);pose.popPose();}
                }else{
                    var mob=machine.containedMob();
                    if(mob!=null){
                        pose.pushPose();pose.translate(.5,5f/48,.5);pose.scale(1f/3,1f/3,1f/3);
                        // The vanilla renderer supplies species-specific geometry, textures,
                        // equipment, eye layers and idle/limb animation at actual mob scale.
                        // Invoke it directly to avoid world shadow/hitbox/fire passes for a
                        // machine-owned mob that is not in the world's entity collection.
                        if(machine.program()==28)pose.scale(.4f,.4f,.4f);
                        mc.getEntityRenderDispatcher().getRenderer(mob).render(mob,180,partial,pose,buffers,mob.getType()==net.minecraft.world.entity.EntityType.BLAZE?LightTexture.FULL_BRIGHT:light);
                        pose.popPose();
                    }
                }
            }
            pose.popPose();
        }
        private void renderCrop(FarmMachine machine,net.minecraft.world.level.block.state.BlockState crop,float progress,float phase,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            if(machine.program()==19){
                pose.pushPose();pose.translate(.5,0,.5);float scale=.25f+.75f*progress;pose.scale(scale,scale,scale);pose.translate(-.5,0,-.5);
                draw("salt_crystal_growth",pose,buffers,light,overlay);pose.popPose();return;
            }
            if(machine.program()==15){
                pose.pushPose();pose.translate(.5,0,.5);
                float current=(float)Math.sin((machine.getLevel().getGameTime()+partial)*.07+phase)*3;
                pose.mulPose(Axis.ZP.rotationDegrees(current));pose.scale(.85f,.4f+.5f*progress,.85f);pose.translate(-.5,0,-.5);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crop,pose,buffers,light,overlay);pose.popPose();return;
            }
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(crop,pose,buffers,light,overlay);
        }
        @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(FarmMachine m){return new net.minecraft.world.phys.AABB(m.getBlockPos()).inflate(m.kind().realTree()?7:4);}
        @Override public boolean shouldRenderOffScreen(FarmMachine m){return true;}
    }
}
