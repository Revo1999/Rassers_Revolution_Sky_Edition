package dk.revo.revoindustry.feeding;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public final class FeedingHelmetItem extends ArmorItem {
    public final int tier;
    public FeedingHelmetItem(int tier) {
        super(tier == 2 ? ArmorMaterials.DIAMOND : ArmorMaterials.IRON, Type.HELMET,
            new Item.Properties().durability(new int[]{165, 275, 363}[tier]).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        this.tier = tier;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return super.use(level, player, hand);
        if (player instanceof ServerPlayer server) FeedingHelmets.open(server, hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player && player.tickCount % 20 == 0) FeedingHelmets.feed(player, stack);
    }
    @Override public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ResourceLocation.fromNamespaceAndPath("revoindustry", "textures/models/armor/" + FeedingHelmets.IDS[tier] + ".png");
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.revoindustry.feeding_capacity", FeedingHelmets.capacity(tier)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.revoindustry.feeding_use").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.revoindustry.feeding_smart").withStyle(ChatFormatting.DARK_GREEN));
    }
}
