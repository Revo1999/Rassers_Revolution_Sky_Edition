package dk.revo.revocultivation.client;
import java.util.*;
import dk.revo.revocultivation.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ComposterBlock;
public final class SmallMachineEmi {
 static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,s);}
 public static void open(boolean silk){var block=silk?SmallMachines.CULTIVATOR.get():SmallMachines.COMPOSTER.get();EmiApi.displayRecipe(new Production(new EmiRecipeCategory(id(silk?"silkworm_cultivation":"composting"),EmiStack.of(block)),silk));}
 public static void register(EmiRegistry r){for(boolean silk:new boolean[]{false,true}){var block=silk?SmallMachines.CULTIVATOR.get():SmallMachines.COMPOSTER.get();var cat=new EmiRecipeCategory(id(silk?"silkworm_cultivation":"composting"),EmiStack.of(block));r.addCategory(cat);r.addWorkstation(cat,EmiStack.of(block));r.addRecipe(new Production(cat,silk));}}
 record Production(EmiRecipeCategory category,boolean silk) implements EmiRecipe {
  public EmiRecipeCategory getCategory(){return category;}
  public ResourceLocation getId(){return id("production/"+(silk?"cocoons":"compost"));}
  public List<EmiIngredient> getInputs(){var choices=BuiltInRegistries.ITEM.stream().filter(i->silk?new ItemStack(i).is(ItemTags.LEAVES):i!=SmallMachines.COMPOST.get()&&ComposterBlock.COMPOSTABLES.getFloat(i)>0).map(i->EmiStack.of(i,silk?2:4)).toList();return List.of(EmiIngredient.of(choices));}
  public List<EmiIngredient> getCatalysts(){return silk?List.of(EmiStack.of(SmallMachines.WORM.get())):List.of();}
  public List<EmiStack> getOutputs(){return List.of(EmiStack.of(silk?SmallMachines.COCOON.get():SmallMachines.COMPOST.get(),silk?2:1));}
  public int getDisplayWidth(){return 170;}public int getDisplayHeight(){return 80;}public boolean supportsRecipeTree(){return false;}
  public void addWidgets(WidgetHolder w){w.addSlot(getInputs().getFirst(),8,8);if(silk)w.addSlot(EmiStack.of(SmallMachines.WORM.get()),30,8);w.addText(Component.literal(">"),73,14,0x404040,false);w.addSlot(getOutputs().getFirst(),108,8).recipeContext(this);w.addText(Component.translatable(silk?"recipe.revocultivation.worm":"recipe.revocultivation.compost_time"),4,36,0x404040,false);w.addText(Component.translatable("recipe.revocultivation.passive_time"),4,52,0x555555,false);w.addText(Component.translatable("recipe.revocultivation.small_output"),4,66,0x555555,false);}
 }
}

