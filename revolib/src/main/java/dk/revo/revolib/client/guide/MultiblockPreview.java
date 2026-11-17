package dk.revo.revolib.client.guide;

import java.util.*;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/** Reusable GUI-space block preview. No world edits, packets or dependency on EMI/MI. */
public final class MultiblockPreview {
    public final MultiblockGuide guide;
    private float yaw=35,pitch=25,zoom=1;
    private int layer;
    private boolean single,dragging;
    private double lastX,lastY;
    private int highlight;
    private MultiblockGuide.Part hovered;
    public MultiblockPreview(MultiblockGuide guide){this.guide=guide;layer=guide.layers()-1;}
    public int layer(){return layer;}
    public boolean single(){return single;}
    public void layer(int delta){layer=Math.clamp(layer+delta,0,guide.layers()-1);}
    public void toggleSingle(){single=!single;}
    public void zoom(double delta){zoom=(float)Math.clamp(zoom*Math.pow(1.12,delta),.55,1.8);}
    public void reset(){yaw=35;pitch=25;zoom=1;dragging=false;}
    public void rotate(float delta){yaw+=delta;}
    public void cycleHighlight(){highlight=(highlight+1)%(guide.parts().stream().anyMatch(p->(p.role()==MultiblockGuide.Role.POWER||p.role()==MultiblockGuide.Role.ELECTRIC_INPUT||p.role()==MultiblockGuide.Role.ENERGY_INPUT||p.role()==MultiblockGuide.Role.FUEL_INPUT))?5:4);}
    public String highlightLabel(){return switch(highlight){case 1->"All ports";case 2->"Controller";case 3->"Item output";case 4->"Power input";default->"Ports: off";};}
    public void click(double mouseX,double mouseY,int button){if(button==1)reset();else if(button==0){dragging=true;lastX=mouseX;lastY=mouseY;}}
    private boolean selected(MultiblockGuide.Part part){return switch(highlight){case 1->part.role()!=MultiblockGuide.Role.BLOCK;case 2->part.role()==MultiblockGuide.Role.CONTROLLER;case 3->part.role()==MultiblockGuide.Role.OUTPUT||part.role()==MultiblockGuide.Role.INPUT_OUTPUT;case 4->part.role()==MultiblockGuide.Role.POWER||part.role()==MultiblockGuide.Role.ELECTRIC_INPUT||part.role()==MultiblockGuide.Role.ENERGY_INPUT||part.role()==MultiblockGuide.Role.FUEL_INPUT;default->false;};}
    public List<Component> tooltip(){
        if(hovered==null)return List.of();
        var p=hovered.offset();
        return List.of(hovered.state().getBlock().getName(),Component.literal(hovered.role().label),Component.literal("From controller: X "+p.getX()+", Y "+p.getY()+", Z "+p.getZ()));
    }
    public void render(GuiGraphics g,int x,int y,int width,int height,int mouseX,int mouseY){
        var mc=Minecraft.getInstance();
        if(dragging){
            if(GLFW.glfwGetMouseButton(mc.getWindow().getWindow(),GLFW.GLFW_MOUSE_BUTTON_LEFT)!=GLFW.GLFW_PRESS)dragging=false;
            else {yaw+=(mouseX-lastX)*.65f;pitch=(float)Math.clamp(pitch+(mouseY-lastY)*.5,5,80);lastX=mouseX;lastY=mouseY;}
        }
        g.fill(x,y,x+width,y+height,0xFF343A3D);
        int minX=guide.parts().stream().mapToInt(p->p.offset().getX()).min().orElse(0);
        int minZ=guide.parts().stream().mapToInt(p->p.offset().getZ()).min().orElse(0);
        float scale=Math.min(width/(guide.sizeX()+guide.sizeZ()+1.5f),(height-16)/(guide.layers()+2.7f))*zoom;
        Matrix4f transform=new Matrix4f().translate(x+width*.5f,y+height*.54f,150)
            .scale(scale,-scale,scale).rotateX((float)Math.toRadians(pitch)).rotateY((float)Math.toRadians(yaw))
            .translate(-minX-guide.sizeX()*.5f,-guide.minY()-guide.layers()*.5f,-minZ-guide.sizeZ()*.5f);
        hovered=null;
        Vector3f start=new Vector3f(mouseX,mouseY,1000),end=new Vector3f(mouseX,mouseY,-1000);
        Matrix4f inverse=new Matrix4f(transform).invert();inverse.transformPosition(start);inverse.transformPosition(end);
        Vec3 from=new Vec3(start.x,start.y,start.z),to=new Vec3(end.x,end.y,end.z);
        double nearest=Double.MAX_VALUE;
        boolean inside=mouseX>=x&&mouseX<x+width&&mouseY>=y&&mouseY<y+height;
        var visible=guide.parts().stream().filter(p->!p.state().isAir()&&(single?p.offset().getY()==guide.minY()+layer:p.offset().getY()<=guide.minY()+layer)).toList();
        if(inside)for(var part:visible){var hit=new AABB(part.offset()).clip(from,to);if(hit.isPresent()&&from.distanceToSqr(hit.get())<nearest){nearest=from.distanceToSqr(hit.get());hovered=part;}}
        g.flush();
        // EMI translates the pose to recipe space; scissor coordinates are screen-space.
        Vector3f clipMin=g.pose().last().pose().transformPosition(new Vector3f(x,y,0));
        Vector3f clipMax=g.pose().last().pose().transformPosition(new Vector3f(x+width,y+height,0));
        g.enableScissor((int)clipMin.x,(int)clipMin.y,(int)clipMax.x,(int)clipMax.y);
        Lighting.setupFor3DItems();
        var pose=g.pose();pose.pushPose();pose.mulPose(transform);
        var buffers=mc.renderBuffers().bufferSource();
        for(var part:visible){
            var p=part.offset();pose.pushPose();pose.translate(p.getX(),p.getY(),p.getZ());
            float shade=highlight>0&&!selected(part)?.32f:1f;
            var model=mc.getBlockRenderer().getBlockModel(part.state());
            if(part.modelData()!=net.neoforged.neoforge.client.model.data.ModelData.EMPTY){
                model=new net.neoforged.neoforge.client.model.BakedModelWrapper<>(model){
                    @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random){return originalModel.getQuads(state,side,random,part.modelData(),null);}
                    @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random,net.neoforged.neoforge.client.model.data.ModelData ignored,net.minecraft.client.renderer.RenderType renderType){return originalModel.getQuads(state,side,random,part.modelData(),renderType);}
                };
            }
            VertexConsumer vertices=buffers.getBuffer(Sheets.cutoutBlockSheet());
            if(shade<1)vertices=new DimmedVertices(vertices,shade);
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),vertices,part.state(),model,1,1,1,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            if(selected(part)||part==hovered)LevelRenderer.renderLineBox(pose,buffers.getBuffer(RenderType.lines()),new AABB(-.005,-.005,-.005,1.005,1.005,1.005),selected(part)?.3f:1f,1f,selected(part)?.45f:1f,1f);
            pose.popPose();
        }
        buffers.endBatch();pose.popPose();g.disableScissor();
        Lighting.setupFor3DItems();
        g.drawString(mc.font,"Drag: rotate | RMB: reset",x+5,y+height-11,0xFFD5DBDA,false);
    }
    private record DimmedVertices(VertexConsumer delegate,float shade) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor((int)(r*shade),(int)(g*shade),(int)(b*shade),a);return this;}
        public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
    }
}
