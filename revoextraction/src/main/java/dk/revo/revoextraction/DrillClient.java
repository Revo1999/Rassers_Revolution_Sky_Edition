package dk.revo.revoextraction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid=RevoExtraction.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DrillClient {
    private static ModelResourceLocation model(DrillTier tier,String part) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"block/"+tier.prefix()+"_"+part));
    }
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(RevoExtraction.DRILL_MENU.get(),DrillScreen::new);
        event.register(RevoExtraction.HATCH_MENU.get(),DrillHatchScreen::new);
    }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) {
        for(DrillTier tier:DrillTier.values()) for(String part:new String[]{"body","drill_bit","drive_gear"}) event.register(model(tier,part));
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RevoExtraction.MACHINE.get(),Renderer::new);
        event.registerBlockEntityRenderer(RevoExtraction.WATER_TANK_ENTITY.get(),TankRenderer::new);
    }
    public static class TankRenderer implements BlockEntityRenderer<DrillWaterTankEntity>{
        public TankRenderer(BlockEntityRendererProvider.Context context){}
        @Override public void render(DrillWaterTankEntity tank,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){DrillCoolantRenderer.renderTank(tank,pose,buffers,light,overlay);}
    }
    public static class Renderer implements BlockEntityRenderer<DrillMachine> {
        public Renderer(BlockEntityRendererProvider.Context context) {}
        @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(DrillMachine machine) {
            return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(3);
        }
        private void draw(DrillTier tier,String part,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var mc=Minecraft.getInstance();
            var baked=mc.getModelManager().getModel(model(tier,part));
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,baked,1,1,1,light,overlay);
        }
        @Override public void render(DrillMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            if(!machine.formed()) return;
            var origin=machine.getBlockPos();
            var center=machine.at(0,0,0);
            pose.pushPose();
            pose.translate(center.getX()-origin.getX()+.5,center.getY()-origin.getY(),center.getZ()-origin.getZ()+.5);
            pose.mulPose(Axis.YP.rotationDegrees(180-machine.facing().toYRot()));
            pose.translate(-1.5,0,-1.5);
            pose.scale(3,3,3);
            draw(machine.tier(),"body",pose,buffers,light,overlay);
            if(machine.tier()==DrillTier.STEEL)DrillCoolantRenderer.render(machine,pose,buffers,light,overlay);
            float angle=machine.previousClientAngle+(machine.clientAngle-machine.previousClientAngle)*partial;
            pose.pushPose();
            pose.translate(.5,.375,.5);
            pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.translate(-.5,-.375,-.5);
            draw(machine.tier(),"drill_bit",pose,buffers,light,overlay);
            pose.popPose();
            pose.pushPose();
            pose.translate(46.0/48,24.0/48,.5);
            pose.mulPose(Axis.XP.rotationDegrees(-angle));
            pose.translate(-46.0/48,-24.0/48,-.5);
            draw(machine.tier(),"drive_gear",pose,buffers,light,overlay);
            pose.popPose();
            pose.popPose();
        }
        @Override public boolean shouldRenderOffScreen(DrillMachine machine) { return true; }
    }
}

