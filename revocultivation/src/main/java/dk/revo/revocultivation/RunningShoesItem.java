package dk.revo.revocultivation;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class RunningShoesItem extends ArmorItem {
    private static final ItemAttributeModifiers EFFECTS=ItemAttributeModifiers.builder()
        .add(Attributes.MOVEMENT_SPEED,new AttributeModifier(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"running_shoes_speed"),.25,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),EquipmentSlotGroup.FEET)
        .add(Attributes.FALL_DAMAGE_MULTIPLIER,new AttributeModifier(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"running_shoes_cushioning"),-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),EquipmentSlotGroup.FEET).build();
    public RunningShoesItem(Holder<ArmorMaterial> material){super(material,Type.BOOTS,new Item.Properties().durability(256));}
    @Override public ItemAttributeModifiers getDefaultAttributeModifiers(){return EFFECTS;}
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,context,lines,flag);
        lines.add(Component.translatable("tooltip.revocultivation.running_shoes").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.revocultivation.running_shoes_fall").withStyle(ChatFormatting.AQUA));
    }
}
