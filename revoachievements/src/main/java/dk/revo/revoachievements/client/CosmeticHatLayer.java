package dk.revo.revoachievements.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;

/** Cosmetic-only layer: renders the installed hat item's normal HEAD model, never equips an item. */
public final class CosmeticHatLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public CosmeticHatLayer(PlayerRenderer renderer){super(renderer);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float swing,float amount,float partial,float age,float yaw,float pitch){
        if(player.isInvisible())return;
        // A physical Simple Hats helmet retains priority, avoiding two overlapping hat models.
        var head=player.getItemBySlot(EquipmentSlot.HEAD);
        if(!head.isEmpty()&&BuiltInRegistries.ITEM.getKey(head.getItem()).getNamespace().equals("simplehats"))return;
        var stack=WardrobeClient.hatFor(player);if(stack.isEmpty())return;
        pose.pushPose();pose.scale(1.01f,1.01f,1.01f);getParentModel().getHead().translateAndRotate(pose);
        CustomHeadLayer.translateToHead(pose,false);
        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player,stack,ItemDisplayContext.HEAD,false,pose,buffers,light);
        pose.popPose();
    }
}
