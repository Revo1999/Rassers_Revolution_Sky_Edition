package dk.revo.revoextraction;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=RevoExtraction.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class SluiceClient {
    static ModelResourceLocation model(String part) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID, "block/sluice/" + part));
    }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) {
        for (var name : new String[]{"drive", "crank", "rod", "shaker", "trough", "spring_front", "spring_rear"}) event.register(model(name));
    }
    @SubscribeEvent public static void renderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SluiceContent.MACHINE.get(), Renderer::new);
    }
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent event) {
        event.register(SluiceContent.CONTROL_MENU.get(), Screen::new);
        event.register(SluiceContent.OUTPUT_MENU.get(), Screen::new);
        event.register(SluiceContent.FEED_MENU.get(), Screen::new);
    }
    public static final class Screen extends AbstractContainerScreen<SluiceMenu> {
        public Screen(SluiceMenu menu, Inventory inv, Component title) {
            super(menu, inv, title); imageWidth = menu.kind == SluiceMenu.Kind.CONTROL ? 230 : 176;
            imageHeight = menu.kind == SluiceMenu.Kind.CONTROL ? 154 : 166;
            inventoryLabelY = imageHeight - 94;
        }
        @Override protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
            int x=leftPos, y=topPos;
            g.fill(x,y,x+imageWidth,y+imageHeight,0xffbabbb4);
            g.fill(x+2,y+2,x+imageWidth-2,y+imageHeight-2,0xffd0d0c7);
            for (var slot : menu.slots) {
                g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff555b54);
                g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff92988c);
            }
            if (menu.kind == SluiceMenu.Kind.CONTROL) {
                line(g,"Shaft: "+menu.data.get(0)+" RPM",12,30);
                line(g,Component.translatable("gui.revoextraction.sluice_status_"+menu.data.get(4)),12,48);
                line(g,"Water: "+menu.data.get(2)+" / 4000 mB",12,68);
                line(g,"Gravel "+menu.data.get(3)+" | Work "+menu.data.get(1)+"%",12,84);
                g.drawWordWrap(font,Component.literal("Pipe at upper rear. Shaft on either side. Collect at the far end."),x+12,y+105,204,0xff343b33);
                RevoScreenBrand.draw(g,font,x+imageWidth-13,y+imageHeight-13);
            } else if (menu.kind == SluiceMenu.Kind.FEEDER) {
                line(g,"Water: "+menu.data.get(2)+" mB",8,22);
                line(g,menu.data.get(6)==1?"Top: gravel":"Place above inlet",8,34);
                line(g,"Rear pipe",8,50);
            } else line(g,"Automatic output",8,57);
        }
        void line(GuiGraphics g, String text, int x, int y) { line(g,Component.literal(text),x,y); }
        void line(GuiGraphics g, Component text, int x, int y) { g.drawString(font,text,leftPos+x,topPos+y,0xff343b33,false); }
        @Override protected void renderLabels(GuiGraphics g,int mx,int my) {
            g.drawString(font,title,titleLabelX,titleLabelY,0xff343b33,false);
            if (menu.kind != SluiceMenu.Kind.CONTROL) g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xff343b33,false);
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partial) { super.render(g,mx,my,partial); renderTooltip(g,mx,my); }
    }
    public static final class Renderer implements BlockEntityRenderer<SluiceEntity> {
        public Renderer(BlockEntityRendererProvider.Context context) {}
        @Override public AABB getRenderBoundingBox(SluiceEntity e) { return new AABB(e.getBlockPos()).inflate(4); }
        @Override public boolean shouldRenderOffScreen(SluiceEntity e) { return true; }
        void draw(String name,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var mc=Minecraft.getInstance(); var baked=mc.getModelManager().getModel(model(name));
            pose.pushPose();pose.scale(3,3,3);
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),null,baked,1,1,1,light,overlay);
            pose.popPose();
        }
        void pivot(PoseStack p,float x,float y,float z,float angle) {
            p.translate((x+8)/16f,y/16f,z/16f);p.mulPose(Axis.XP.rotationDegrees(angle));p.translate(-(x+8)/16f,-y/16f,-z/16f);
        }
        @Override public void render(SluiceEntity e,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            if (!e.assembled()) return;
            pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-e.downstream().toYRot()));pose.translate(-.5,0,-.5);
            float angle=e.previousClientAngle+(e.clientAngle-e.previousClientAngle)*partial;
            var motion=SluiceMotion.at(Math.toRadians(angle));
            pose.pushPose();pivot(pose,0,8,8,-angle);draw("drive",pose,buffers,light,overlay);pose.popPose();
            pose.pushPose();pivot(pose,-10,8,8,-angle);draw("crank",pose,buffers,light,overlay);pose.popPose();
            for(String spring:new String[]{"spring_front","spring_rear"}){
                double anchor=spring.equals("spring_front")?5.6:1.6;
                pose.pushPose();pose.translate(0,anchor/16,0);pose.scale(1,(float)motion.springScale(),1);pose.translate(0,-anchor/16,0);
                draw(spring,pose,buffers,light,overlay);pose.popPose();
            }
            pose.pushPose();pose.translate(0,motion.lift()/16,0);
            draw("rod",pose,buffers,light,overlay);
            draw("trough",pose,buffers,light,overlay);
            draw("shaker",pose,buffers,light,overlay);
            var feeder=e.feeder();
            if (feeder!=null&&!feeder.input.getStackInSlot(0).isEmpty()) {
                for (int i=0;i<5;i++) {
                    pose.pushPose();pose.translate((5+(i*3)%7)/16f,14.4/16f,(7+i*1.7)/16f);pose.scale(.13f,.13f,.13f);
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRAVEL.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                }
            }
            if(e.processing){
                for(int i=0;i<5;i++){
                    double phase=(e.clientTransport+partial*Math.min(Math.abs(e.getSpeed()),128)/SluiceEntity.WORK_PER_ITEM+i*.2)%1;
                    double z=13+33*phase,y=z<16?13.1:z<32?11.1:9.1;
                    pose.pushPose();pose.translate((4+(i*3)%7)/16f,y/16,z/16);pose.scale(.1f,.1f,.1f);
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRAVEL.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                }
            }
            pose.popPose();
            if (feeder!=null&&!feeder.input.getStackInSlot(0).isEmpty()) {
                pose.pushPose();pose.translate(3.5/16,27.5/16,5.5/16);pose.scale(.55f,.06f,.45f);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRAVEL.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                if (e.processing) {
                    float t=((e.getLevel().getGameTime()+partial)%12)/12f;
                    pose.pushPose();pose.translate(7f/16,(24-6*t)/16,15f/16);pose.scale(.1f,.1f,.1f);
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRAVEL.defaultBlockState(),pose,buffers,light,overlay);pose.popPose();
                }
            }
            if(e.flowing){water(e,partial,pose,buffers,light,overlay);inlet(e,pose,buffers,light,overlay);}
            pose.popPose();
        }
        void water(SluiceEntity e,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("block/water_flow"));
            var v=buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
            int tint=BiomeColors.getAverageWaterColor(e.getLevel(),e.getBlockPos());
            float scroll=(float)((e.getLevel().getGameTime()+partial)*.35%16);
            for(int i=0;i<3;i++) {
                float y=13-i*2,z=i*16;
                for(var strip:SluiceFlow.strips(scroll)) {
                    quad(v,pose,sprite,tint,light,overlay,0,1,0,0,strip.v0(),12f/16,strip.v1(),
                        new float[][]{{2,y,z+strip.end()},{14,y,z+strip.end()},{14,y,z+strip.start()},{2,y,z+strip.start()}});
                }
                if(i<2)quad(v,pose,sprite,tint,light,overlay,0,0,1,0,0,.75f,.17f,
                    new float[][]{{2,y-2,z+16.05f},{14,y-2,z+16.05f},{14,y,z+16.05f},{2,y,z+16.05f}});
            }
            quad(v,pose,sprite,tint,light,overlay,0,0,1,0,0,.75f,.6f,new float[][]{{2,0,48.05f},{14,0,48.05f},{14,9,48.05f},{2,9,48.05f}});
        }
        void inlet(SluiceEntity e,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("block/water_flow"));
            var v=buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
            int tint=BiomeColors.getAverageWaterColor(e.getLevel(),e.getBlockPos());
            float bottom=13;
            quad(v,pose,sprite,tint,light,overlay,0,0,1,0,0,.25f,.5f,new float[][]{{6,bottom,14.6f},{10,bottom,14.6f},{10,18,14.6f},{6,18,14.6f}});
        }
        void quad(VertexConsumer out,PoseStack pose,TextureAtlasSprite sprite,int tint,int light,int overlay,float nx,float ny,float nz,float u0,float v0,float u1,float v1,float[][] points) {
            float[][] uv={{u0,v1},{u1,v1},{u1,v0},{u0,v0}};
            for(int i=0;i<4;i++)out.addVertex(pose.last().pose(),points[i][0]/16,points[i][1]/16,points[i][2]/16)
                .setColor((tint>>16)&255,(tint>>8)&255,tint&255,205).setUv(sprite.getU(uv[i][0]),sprite.getV(uv[i][1]))
                .setOverlay(overlay).setLight(light).setNormal(pose.last(),nx,ny,nz);
        }
    }
}
