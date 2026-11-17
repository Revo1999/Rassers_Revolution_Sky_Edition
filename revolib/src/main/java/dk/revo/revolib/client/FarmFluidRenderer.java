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
        // Tile every block. UVs outside [0,1] would sample neighboring atlas sprites.
        float step=16/divisor;
        for(float x=left;x<right;){float x1=Math.min(right,x+step);
            for(float z=front;z<back;){float z1=Math.min(back,z+step);
                quad(vertices,pose,sprite,tint,light,overlay,0,1,0,(x1-x)/step,(z1-z)/step,x,top,z,x,top,z1,x1,top,z1,x1,top,z);z=z1;
            }x=x1;
        }
        for(float y0=bottom;y0<top;){
            float y1=Math.min(top,y0+step),segment=(y1-y0)/step;
            for(float x=left;x<right;){float x1=Math.min(right,x+step),width=(x1-x)/step;
                quad(vertices,pose,sprite,tint,light,overlay,0,0,-1,width,segment,x1,y0,front,x,y0,front,x,y1,front,x1,y1,front);
                quad(vertices,pose,sprite,tint,light,overlay,0,0,1,width,segment,x,y0,back,x1,y0,back,x1,y1,back,x,y1,back);x=x1;
            }
            for(float z=front;z<back;){float z1=Math.min(back,z+step),depth=(z1-z)/step;
                quad(vertices,pose,sprite,tint,light,overlay,-1,0,0,depth,segment,left,y0,z,left,y0,z1,left,y1,z1,left,y1,z);
                quad(vertices,pose,sprite,tint,light,overlay,1,0,0,depth,segment,right,y0,z1,right,y0,z,right,y1,z,right,y1,z1);z=z1;
            }
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
