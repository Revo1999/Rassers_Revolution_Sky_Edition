package dk.revo.revostorage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

final class SpringRenderer implements BlockEntityRenderer<SpringEntity> {
    static final ModelResourceLocation COIL = model("coil");

    SpringRenderer(BlockEntityRendererProvider.Context context) {}

    private static ModelResourceLocation model(String part) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(RevoStorage.MOD_ID, "block/spring_module_" + part));
    }

    @Override public void render(SpringEntity spring, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - spring.getBlockState().getValue(StorageBlock.FACING).toYRot()));
        pose.translate(-.5, 0, -.5);

        pose.pushPose();
        double rearAnchor = 13 / 16d;
        pose.translate(0, 0, rearAnchor);
        pose.scale(1, 1, 1 - .48f * spring.compression(partial));
        pose.translate(0, 0, -rearAnchor);
        draw(COIL, pose, buffers, light, overlay);
        pose.popPose();

        pose.popPose();
    }

    private static void draw(ModelResourceLocation key, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getModelManager().getModel(key);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(RenderType.solid()),
                null, model, 1, 1, 1, light, overlay);
    }
}
