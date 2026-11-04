package dk.revo.revoextraction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/** Real atlas-sampled fluid behind the tank's cutout glass; resource packs and water animation apply. */
final class DrillCoolantRenderer {
    private DrillCoolantRenderer(){}
    static void render(DrillMachine machine,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        volume(machine.getLevel(),machine.at(-1,1,0),machine.waterAmount(),pose,buffers,light,overlay,new float[]{2.2f,19.6f,19.2f},new float[]{10.8f,32.8f,28.8f},48);
    }
    static void renderTank(DrillWaterTankEntity tank,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(tank.getBlockState().getValue(DrillPart.FORMED))return;
        volume(tank.getLevel(),tank.getBlockPos(),tank.water.getFluidAmount(),pose,buffers,light,overlay,new float[]{2.2f,2.1f,2.2f},new float[]{13.8f,13.9f,13.8f},16);
    }
    private static void volume(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,int amount,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[] low,float[] high,float divisor){
        if(amount<=0)return;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("block/water_still"));
        var vertices=buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        int tint=BiomeColors.getAverageWaterColor(level,pos);
        float left=low[0]/divisor,right=high[0]/divisor,front=low[2]/divisor,back=high[2]/divisor,bottom=low[1]/divisor;
        float top=(low[1]+(high[1]-low[1])*amount/DrillWaterTankEntity.CAPACITY)/divisor;
        float height=(top-bottom)*divisor/16,width=(high[0]-low[0])/16,depth=(high[2]-low[2])/16;
        quad(vertices,pose,sprite,tint,light,overlay,0,1,0,width,depth,new float[][]{{left,top,front},{left,top,back},{right,top,back},{right,top,front}});
        quad(vertices,pose,sprite,tint,light,overlay,0,0,-1,width,height,new float[][]{{right,bottom,front},{left,bottom,front},{left,top,front},{right,top,front}});
        quad(vertices,pose,sprite,tint,light,overlay,0,0,1,width,height,new float[][]{{left,bottom,back},{right,bottom,back},{right,top,back},{left,top,back}});
        quad(vertices,pose,sprite,tint,light,overlay,-1,0,0,depth,height,new float[][]{{left,bottom,front},{left,bottom,back},{left,top,back},{left,top,front}});
        quad(vertices,pose,sprite,tint,light,overlay,1,0,0,depth,height,new float[][]{{right,bottom,back},{right,bottom,front},{right,top,front},{right,top,back}});
    }
    private static void quad(VertexConsumer out,PoseStack pose,TextureAtlasSprite sprite,int tint,int light,int overlay,float nx,float ny,float nz,float width,float height,float[][] points){
        float[][] uv={{0,height},{width,height},{width,0},{0,0}};
        for(int i=0;i<4;i++)out.addVertex(pose.last().pose(),points[i][0],points[i][1],points[i][2])
            .setColor((tint>>16)&255,(tint>>8)&255,tint&255,200)
            .setUv(sprite.getU(uv[i][0]),sprite.getV(uv[i][1]))
            .setOverlay(overlay).setLight(light).setNormal(pose.last(),nx,ny,nz);
    }
}
