package dk.revo.revolib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/** Vanilla animated, biome-tinted water inside a host model's specified physical bounds. */
public final class FarmFluidRenderer {
    private FarmFluidRenderer(){}
    public static void water(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,int amount,int capacity,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[] low,float[] high,float divisor){
        if(amount<=0)return;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("block/water_still"));
        var vertices=buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        int tint=BiomeColors.getAverageWaterColor(level,pos);
        float left=low[0]/divisor,right=high[0]/divisor,front=low[2]/divisor,back=high[2]/divisor,bottom=low[1]/divisor;
        float top=(low[1]+(high[1]-low[1])*amount/capacity)/divisor;
        float height=(top-bottom)*divisor/16,width=(high[0]-low[0])/16,depth=(high[2]-low[2])/16;
        quad(vertices,pose,sprite,tint,light,overlay,0,1,0,width,depth,left,top,front,left,top,back,right,top,back,right,top,front);
        for(float y0=bottom;y0<top;){
            float y1=Math.min(top,y0+16/divisor),segment=(y1-y0)*divisor/16;
            quad(vertices,pose,sprite,tint,light,overlay,0,0,-1,width,segment,right,y0,front,left,y0,front,left,y1,front,right,y1,front);
            quad(vertices,pose,sprite,tint,light,overlay,0,0,1,width,segment,left,y0,back,right,y0,back,right,y1,back,left,y1,back);
            quad(vertices,pose,sprite,tint,light,overlay,-1,0,0,depth,segment,left,y0,front,left,y0,back,left,y1,back,left,y1,front);
            quad(vertices,pose,sprite,tint,light,overlay,1,0,0,depth,segment,right,y0,back,right,y0,front,right,y1,front,right,y1,back);
            y0=y1;
        }
    }
    private static void quad(VertexConsumer out,PoseStack pose,TextureAtlasSprite sprite,int tint,int light,int overlay,float nx,float ny,float nz,float width,float height,float x0,float y0,float z0,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3){
        vertex(out,pose,sprite,tint,light,overlay,nx,ny,nz,x0,y0,z0,0,height);
        vertex(out,pose,sprite,tint,light,overlay,nx,ny,nz,x1,y1,z1,width,height);
        vertex(out,pose,sprite,tint,light,overlay,nx,ny,nz,x2,y2,z2,width,0);
        vertex(out,pose,sprite,tint,light,overlay,nx,ny,nz,x3,y3,z3,0,0);
    }
    private static void vertex(VertexConsumer out,PoseStack pose,TextureAtlasSprite sprite,int tint,int light,int overlay,float nx,float ny,float nz,float x,float y,float z,float u,float v){
        out.addVertex(pose.last().pose(),x,y,z).setColor((tint>>16)&255,(tint>>8)&255,tint&255,200)
            .setUv(sprite.getU(u),sprite.getV(v)).setOverlay(overlay).setLight(light).setNormal(pose.last(),nx,ny,nz);
    }
}
