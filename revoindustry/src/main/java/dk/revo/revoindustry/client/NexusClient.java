package dk.revo.revoindustry.client;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.nexus.*;
import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NexusClient implements BlockEntityRenderer<NexusMachine> {
    private static final ModelResourceLocation SURFACE=ModelResourceLocation.standalone(Nexus.id("block/nexus_portal_surface"));
    private final BlockEntityRendererProvider.Context context;
    private final java.util.Map<String,MultiblockMachineBER> machines=new java.util.HashMap<>();
    public NexusClient(BlockEntityRendererProvider.Context context){this.context=context;}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){e.register(SURFACE);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Nexus.ENTITY.get(),NexusClient::new);}
    @Override public void render(NexusMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        machines.computeIfAbsent(machine.kind,k->new MultiblockMachineBER(context)).render(machine,partial,pose,buffers,light,overlay);
        if(!machine.portalPowered||!machine.isShapeValid()||!machine.kind.equals("nexus_portal"))return;
        // Only the two-sided animated surface is special-rendered. All frame/deck/rail blocks stay in-world.
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-machine.orientation.facingDirection.toYRot()));pose.translate(-.5,0,-.5);
        pose.translate(-5,4,4.15-5.5);pose.scale(11,11,11);
        var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(SURFACE),1,1,1,LightTexture.FULL_BRIGHT,overlay);
        pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(NexusMachine machine){return true;}
    @Override public AABB getRenderBoundingBox(NexusMachine machine){return new AABB(machine.getBlockPos()).inflate(24);}
}
