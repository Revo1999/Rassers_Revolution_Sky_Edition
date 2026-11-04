package dk.revo.revoextraction;
import java.util.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
@EmiEntrypoint
public class HammerEmiPlugin implements EmiPlugin{
 static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,s);}
 public void register(EmiRegistry r){var c=new EmiRecipeCategory(id("hammering"),EmiStack.of(HammerItems.STONE.get()));r.addCategory(c);r.addWorkstation(c,EmiStack.of(HammerItems.STONE.get()));r.addWorkstation(c,EmiStack.of(HammerItems.WOOD.get()));r.addRecipe(new Recipe(c,"pebbles",Items.DIRT,HammerItems.PEBBLE.get()));r.addRecipe(new Recipe(c,"gravel",Items.COBBLESTONE,Items.GRAVEL));r.addRecipe(new Recipe(c,"sand",Items.GRAVEL,Items.SAND));r.addRecipe(new Recipe(c,"dust",Items.SAND,HammerItems.DUST.get().asItem()));}
 record Recipe(EmiRecipeCategory category,String name,Item input,Item output) implements EmiRecipe{
 public EmiRecipeCategory getCategory(){return category;}public ResourceLocation getId(){return id("hammering/"+name);}
 public List<EmiIngredient> getInputs(){return List.of(EmiStack.of(input));}public List<EmiIngredient> getCatalysts(){return List.of(EmiIngredient.of(List.of(EmiStack.of(HammerItems.WOOD.get()),EmiStack.of(HammerItems.STONE.get()))));}public List<EmiStack> getOutputs(){return List.of(EmiStack.of(output,name.equals("pebbles")?4:1));}
 public boolean supportsRecipeTree(){return false;}public int getDisplayWidth(){return 160;}public int getDisplayHeight(){return 55;}
 public void addWidgets(WidgetHolder w){w.addSlot(EmiStack.of(input),4,4);w.addText(Component.literal(">"),65,10,0x404040,false);w.addSlot(EmiStack.of(output,name.equals("pebbles")?4:1),108,4).recipeContext(this);w.addText(Component.translatable("recipe.revoextraction.hammer"),4,30,0x404040,false);w.addText(Component.translatable("recipe.revoextraction.hammer_use"),4,43,0x555555,false);}
 }
}

