package dk.revo.revocultivation;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Exact 3x3 upgrades preserve the selected mob and its vanilla block-entity component. */
public final class SpawnerUpgradeRecipe extends CustomRecipe {
    private final int targetTier;
    public SpawnerUpgradeRecipe(CraftingBookCategory category,int tier){super(category);targetTier=tier;}
    @Override public boolean matches(CraftingInput input,Level level){
        if(input.width()!=3||input.height()!=3||MobSpawnerPrograms.tier(input.getItem(4))!=targetTier-1)return false;
        String metal=targetTier==2?"steel_plate":"stainless_steel_plate",circuit=targetTier==2?"electronic_circuit":"analog_circuit";
        for(int i=0;i<9;i++){
            if(i==4)continue;
            boolean circuitSlot=targetTier==2?i==7:i==3||i==5;
            var ingredient=BuiltInRegistries.ITEM.get(ResourceLocation.parse("modern_industrialization:"+(circuitSlot?circuit:metal)));
            if(ingredient==net.minecraft.world.item.Items.AIR||!input.getItem(i).is(ingredient))return false;
        }return true;
    }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries){return MobSpawnerPrograms.upgrade(input.getItem(4),targetTier);}
    @Override public boolean canCraftInDimensions(int width,int height){return width>=3&&height>=3;}
    @Override public RecipeSerializer<?> getSerializer(){return targetTier==2?RevoCultivation.REINFORCED_SPAWNER_RECIPE.get():RevoCultivation.ADVANCED_SPAWNER_RECIPE.get();}
}
