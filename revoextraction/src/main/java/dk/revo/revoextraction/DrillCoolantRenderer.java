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
    private static final float[] MACHINE_LOW={2.2f,19.6f,19.2f},MACHINE_HIGH={10.8f,32.8f,28.8f},TANK_LOW={2.2f,2.1f,2.2f},TANK_HIGH={13.8f,13.9f,13.8f};
    private DrillCoolantRenderer(){}
    static void render(DrillMachine machine,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        volume(machine.getLevel(),machine.at(-1,1,0),machine.waterAmount(),pose,buffers,light,overlay,MACHINE_LOW,MACHINE_HIGH,48);
    }
    static void renderTank(DrillWaterTankEntity tank,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(tank.getBlockState().getValue(DrillPart.FORMED))return;
        volume(tank.getLevel(),tank.getBlockPos(),tank.water.getFluidAmount(),pose,buffers,light,overlay,TANK_LOW,TANK_HIGH,16);
    }
    private static void volume(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,int amount,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[] low,float[] high,float divisor){
        if(amount<=0)return;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("block/water_still"));
        var vertices=buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        int tint=BiomeColors.getAverageWaterColor(level,pos);
        float left=low[0]/divisor,right=high[0]/divisor,front=low[2]/divisor,back=high[2]/divisor,bottom=low[1]/divisor;
        float top=(low[1]+(high[1]-low[1])*amount/DrillWaterTankEntity.CAPACITY)/divisor;
        float height=(top-bottom)*divisor/16,width=(high[0]-low[0])/16,depth=(high[2]-low[2])/16;
        quad(vertices,pose,sprite,tint,light,overlay,0,1,0,width,depth,left,top,front,left,top,back,right,top,back,right,top,front);
        quad(vertices,pose,sprite,tint,light,overlay,0,0,-1,width,height,right,bottom,front,left,bottom,front,left,top,front,right,top,front);
        quad(vertices,pose,sprite,tint,light,overlay,0,0,1,width,height,left,bottom,back,right,bottom,back,right,top,back,left,top,back);
        quad(vertices,pose,sprite,tint,light,overlay,-1,0,0,depth,height,left,bottom,front,left,bottom,back,left,top,back,left,top,front);
        quad(vertices,pose,sprite,tint,light,overlay,1,0,0,depth,height,right,bottom,back,right,bottom,front,right,top,front,right,top,back);
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
