package dk.revo.revoachievements;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record Cosmetic(ResourceLocation id, Kind kind, String name, String description,
                       String hint, ResourceLocation item, int color, int order) {
    public enum Kind { TITLE, HAT }
    public ItemStack icon() { return new ItemStack(BuiltInRegistries.ITEM.get(item)); }
    public CompoundTag toTag() {
        var t=new CompoundTag();t.putString("Id",id.toString());t.putString("Kind",kind.name());
        t.putString("Name",name);t.putString("Description",description);t.putString("Hint",hint);
        t.putString("Item",item.toString());t.putInt("Color",color);t.putInt("Order",order);return t;
    }
    public static Cosmetic fromTag(CompoundTag t) {
        return new Cosmetic(ResourceLocation.parse(t.getString("Id")),Kind.valueOf(t.getString("Kind")),
                t.getString("Name"),t.getString("Description"),t.getString("Hint"),
                ResourceLocation.parse(t.getString("Item")),t.getInt("Color"),t.getInt("Order"));
    }
}
