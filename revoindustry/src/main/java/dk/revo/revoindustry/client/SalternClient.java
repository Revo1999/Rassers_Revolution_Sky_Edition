package dk.revo.revoindustry.client;

import com.mojang.blaze3d.vertex.*;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.saltern.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SalternClient implements BlockEntityRenderer<SalternController> {
    public SalternClient(BlockEntityRendererProvider.Context c){}
    public static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(Saltern.id("block/"+name));}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(var n:new String[]{"geode_rock_bed","geode_crystals","geode_electrodes","geode_water_volume"})e.register(model(n));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Saltern.ENTITY.get(),SalternClient::new);}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(Saltern.MENU.get(),SalternScreen::new);}
    @SubscribeEvent public static void fluids(RegisterClientExtensionsEvent e){e.registerFluidType(new IClientFluidTypeExtensions(){public ResourceLocation getStillTexture(){return ResourceLocation.withDefaultNamespace("block/water_still");}public ResourceLocation getFlowingTexture(){return ResourceLocation.withDefaultNamespace("block/water_flow");}public int getTintColor(){return 0xFFC1DCE7;}},Saltern.SALT_WATER_TYPE.get());}
    public boolean shouldRenderOffScreen(SalternController c){return true;}
    public AABB getRenderBoundingBox(SalternController c){return new AABB(c.getBlockPos()).inflate(3);}
    private static void draw(String name,PoseStack pose,MultiBufferSource buffers,RenderType type,int light,int overlay,float r,float g,float b){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(type),null,mc.getModelManager().getModel(model(name)),r,g,b,light,overlay);}
    public void render(SalternController c,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(c.status()==0)return;var p=c.at(0,1,1);pose.pushPose();pose.translate(p.getX()-c.getBlockPos().getX(),1,p.getZ()-c.getBlockPos().getZ());
        int innerLight=LightTexture.FULL_BRIGHT;
        var opaque=RenderType.entityCutoutNoCull(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
        draw("geode_rock_bed",pose,buffers,opaque,light,overlay,1,1,1);
        draw("geode_electrodes",pose,buffers,opaque,light,overlay,1,1,1);
        if(c.activeBatch()){
            double clock=c.progress()+(c.status()==4?partial:0);float growth=.05f+.95f*Math.min(1,(float)clock/320);
            if(clock>350)growth*=Math.max(.04f,1-(clock-350)/50f);
            pose.pushPose();pose.translate(.5,.1,.5);pose.scale(.9f,growth,.9f);pose.translate(-.5,0,-.5);draw("geode_crystals",pose,buffers,opaque,innerLight,overlay,1,1,1);pose.popPose();
            if(c.status()==4&&c.progress()%12<5){
                var v=buffers.getBuffer(RenderType.lightning());var matrix=pose.last().pose();
                float wobble=(float)Math.sin(Math.floor(clock/3)*2.4)*.07f;
                for(float x:new float[]{.08f,.92f}){
                    float middle=(x+.5f)/2+wobble;
                    ribbon(v,matrix,x,.8f,.35f,middle,.55f,.49f);ribbon(v,matrix,middle,.55f,.49f,.5f,.2f+growth*.5f,.53f);
                }
            }
        }
        float height=.88f*c.visibleWater()/1000f;
        if(height>0){
            pose.pushPose();pose.translate(.01,.02,.01);pose.scale(.98f,height,.98f);
            var mc=Minecraft.getInstance();var buffer=new WaterTint(buffers.getBuffer(RenderType.entityTranslucentCull(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)));
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffer,null,mc.getModelManager().getModel(model("geode_water_volume")),1,1,1,LightTexture.FULL_BRIGHT,overlay);pose.popPose();
        }
        pose.popPose();
    }
    private static void ribbon(VertexConsumer v,org.joml.Matrix4f m,float x,float y,float z,float X,float Y,float Z){float w=.013f;v.addVertex(m,x-w,y,z).setColor(125,223,255,230);v.addVertex(m,X-w,Y,Z).setColor(125,223,255,230);v.addVertex(m,X+w,Y,Z).setColor(125,223,255,230);v.addVertex(m,x+w,y,z).setColor(125,223,255,230);}
    /** Native animated water, with aquarium clarity rather than several opaque fluid faces. */
    private static final class WaterTint implements VertexConsumer {
        private final VertexConsumer target;WaterTint(VertexConsumer target){this.target=target;}
        public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){target.setColor((int)(r*.36f),(int)(g*.65f),(int)(b*.90f),(int)(a*.24f));return this;}
        public VertexConsumer setUv(float u,float v){target.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
    }
}
