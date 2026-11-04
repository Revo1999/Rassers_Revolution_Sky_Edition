package dk.revo.revoindustry.client;
import dk.revo.revoindustry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class PrimitiveVesselClient implements BlockEntityRenderer<PrimitiveVesselEntity>{
 public PrimitiveVesselClient(BlockEntityRendererProvider.Context c){}
 static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,"block/"+name));}
 @SubscribeEvent public static void menus(RegisterMenuScreensEvent e){e.register(PrimitiveMachines.MENU.get(),PrimitiveVesselScreen::new);e.register(PrimitiveMachines.CONVERTER_MENU.get(),PowerConverterScreen::new);}
 @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){e.register(model("primitive_water_surface"));e.register(model("primitive_lava_surface"));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(PrimitiveMachines.ENTITY.get(),PrimitiveVesselClient::new);}
 public void render(PrimitiveVesselEntity m,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  var mc=Minecraft.getInstance();boolean rain=m.rain();float floor=rain?2:5,top=floor+(14-floor)*m.tank.getFluidAmount()/1000f;
  if(m.tank.getFluidAmount()>0){pose.pushPose();pose.translate(0,top/16,0);int color=rain?BiomeColors.getAverageWaterColor(m.getLevel(),m.getBlockPos()):0xFFFFFF;mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(rain?RenderType.translucent():RenderType.cutout()),null,mc.getModelManager().getModel(model(rain?"primitive_water_surface":"primitive_lava_surface")),(color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,rain?light:LightTexture.FULL_BRIGHT,overlay);pose.popPose();}
  var item=rain?m.inventory.getStackInSlot(1):m.inventory.getStackInSlot(0);
  if(!item.isEmpty()){pose.pushPose();pose.translate(.5,(top+3)/16f,.5);float scale=rain?.5f:.55f-.2f*m.progress()/200f;pose.scale(scale,scale,scale);mc.getItemRenderer().renderStatic(item,ItemDisplayContext.GROUND,light,overlay,pose,buffers,m.getLevel(),0);pose.popPose();}
 }
}

