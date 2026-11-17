package dk.revo.revoindustry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.hatchery.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class HatcheryClient implements BlockEntityRenderer<HatcheryController> {
    public HatcheryClient(BlockEntityRendererProvider.Context context){}
    private static final ModelResourceLocation[] EGGS={model("wither_egg"),model("wither_egg_cracked_1"),model("wither_egg_cracked_2")};
    private static final ModelResourceLocation ROOF=model("wither_roof_lamp"),LIT_ROOF=model("wither_roof_lamp_lit");
    private static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(Hatchery.id("block/"+name));}
    public static ModelResourceLocation egg(int stage){return EGGS[stage];}
    public static ModelResourceLocation roof(boolean lit){return lit?LIT_ROOF:ROOF;}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(int i=0;i<3;i++)e.register(egg(i));e.register(roof(false));e.register(roof(true));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Hatchery.CONTROLLER_ENTITY.get(),HatcheryClient::new);e.registerEntityRenderer(Hatchery.WITHER.get(),ContainedRenderer::new);}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(Hatchery.MENU.get(),HatcheryScreen::new);}
    @Override public boolean shouldRenderOffScreen(HatcheryController be){return true;}
    @Override public AABB getRenderBoundingBox(HatcheryController be){return new AABB(be.getBlockPos()).inflate(5);}
    public void render(HatcheryController c,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();
        if(c.status()!=0)for(int x=-1;x<=1;x++){
            var lamp=c.at(x,4,2);boolean on=c.stage()>0&&c.status()==5;
            pose.pushPose();pose.translate(lamp.getX()-c.getBlockPos().getX(),4,lamp.getZ()-c.getBlockPos().getZ());
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,mc.getModelManager().getModel(roof(on)),1,1,1,on?LightTexture.FULL_BRIGHT:light,overlay);pose.popPose();
        }
        if(c.stage()!=1)return;
        var centre=c.at(0,1,2);float fraction=Math.clamp(c.progress()/(float)Hatchery.INCUBATION_TICKS,0,1);
        boolean running=c.status()==5;
        double time=c.progress()+(running?partial:0);
        float rock=(float)Math.sin(time*(.09+.20*fraction))*(1+fraction*9);
        float pulse=1+(float)Math.sin(time*.14)*fraction*.035f;
        int crack=fraction<.45?0:fraction<.8?1:2;
        pose.pushPose();pose.translate(centre.getX()-c.getBlockPos().getX()+.5,1.01,centre.getZ()-c.getBlockPos().getZ()+.5);
        pose.mulPose(Axis.ZP.rotationDegrees(rock));pose.mulPose(Axis.YP.rotationDegrees((float)Math.sin(time*.045)*fraction*7));pose.scale(pulse,pulse,pulse);pose.translate(-.5,0,-.5);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,mc.getModelManager().getModel(egg(crack)),1,1,1,crack==2?LightTexture.FULL_BRIGHT:light,overlay);
        pose.popPose();
    }
    private static final class ContainedRenderer extends MobRenderer<ChamberWither,WitherBossModel<ChamberWither>> {
        private static final ResourceLocation TEXTURE=ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");
        ContainedRenderer(EntityRendererProvider.Context c){super(c,new WitherBossModel<>(c.bakeLayer(ModelLayers.WITHER)),.5f);}
        public ResourceLocation getTextureLocation(ChamberWither wither){return TEXTURE;}
    }
}
