package dk.revo.revocultivation.client;
import dk.revo.revocultivation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Contents use real synchronized inventory/work; the wooden body remains chunk-baked. */
@EventBusSubscriber(modid=RevoCultivation.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class SmallMachineRenderer implements BlockEntityRenderer<SmallMachineEntity>{
 public static final String[] MODELS={"cultivator_fill_0","cultivator_fill_1","cultivator_fill_2","cultivator_fill_3","composter_fill_raw","composter_fill_ready","cultivator_live_worm","cultivator_live_cocoons"};
 public SmallMachineRenderer(BlockEntityRendererProvider.Context c){}
 private static ModelResourceLocation model(String n){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"block/"+n));}
 @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(var n:MODELS)e.register(model(n));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(SmallMachines.ENTITY.get(),SmallMachineRenderer::new);}
 private static void draw(String n,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float r,float g,float b){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,mc.getModelManager().getModel(model(n)),r,g,b,light,overlay);}
 public void render(SmallMachineEntity m,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  float fill=m.renderedFill(partial);if(fill<.001f)return;
  boolean silk=m.silk(),ready=m.visualStage()==3;float progress=ready?1:m.processFraction();
  float top=2+(silk?6.5f:12)*fill;
  pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-m.getBlockState().getValue(SmallMachineBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);
  pose.pushPose();pose.translate(0,top/16f,0);
  String surface=silk?"cultivator_fill_"+Math.min(3,(int)(progress*4)):ready?"composter_fill_ready":"composter_fill_raw";
  float tint=silk?1:1-.35f*progress;
  draw(surface,pose,buffers,light,overlay,tint,tint,tint);pose.popPose();
  if(silk&&m.storage.getStackInSlot(1).is(SmallMachines.WORM.get())){pose.pushPose();pose.translate(m.status()==0?Math.sin((m.getLevel().getGameTime()+partial)*.12)*.012:0,(top-7+.08f)/16f,0);draw("cultivator_live_worm",pose,buffers,light,overlay,1,1,1);pose.popPose();}
  if(silk&&(progress>.35f||!m.storage.getStackInSlot(2).isEmpty())){float size=!m.storage.getStackInSlot(2).isEmpty()?1:Math.clamp((progress-.35f)/.65f,.04f,1);pose.pushPose();pose.translate(0,(14f/16)*(1-size),0);pose.scale(1,size,1);draw("cultivator_live_cocoons",pose,buffers,light,overlay,1,1,1);pose.popPose();}
  if(ready){pose.pushPose();pose.translate(.5,(top+1.7f)/16f,.5);pose.scale(.48f,.48f,.48f);Minecraft.getInstance().getItemRenderer().renderStatic(m.storage.getStackInSlot(2),ItemDisplayContext.GROUND,light,overlay,pose,buffers,m.getLevel(),0);pose.popPose();}
  pose.popPose();
 }
}

