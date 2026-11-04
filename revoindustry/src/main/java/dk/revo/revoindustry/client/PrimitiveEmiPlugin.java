package dk.revo.revoindustry.client;
import dk.revo.revoindustry.*;
import java.util.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
@EmiEntrypoint
public class PrimitiveEmiPlugin implements EmiPlugin{
 static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,s);}
 public void register(EmiRegistry r){for(boolean rain:new boolean[]{true,false}){var b=rain?PrimitiveMachines.BARREL.get():PrimitiveMachines.CRUCIBLE.get();var c=new EmiRecipeCategory(id(rain?"rain_barrel":"lava_crucible"),EmiStack.of(b));r.addCategory(c);r.addWorkstation(c,EmiStack.of(b));r.addRecipe(new Recipe(c,rain?0:2));if(rain)r.addRecipe(new Recipe(c,1));}}
 record Recipe(EmiRecipeCategory category,int mode) implements EmiRecipe{
 public EmiRecipeCategory getCategory(){return category;}public ResourceLocation getId(){return id("processing/"+(mode==0?"rain":mode==1?"clay":"lava"));}
 public List<EmiIngredient> getInputs(){return mode==0?List.of():mode==1?List.of(EmiStack.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("revoextraction","dust"))),EmiStack.of(Fluids.WATER,1000)):List.of(EmiStack.of(Blocks.COBBLESTONE,4));}
 public List<EmiIngredient> getCatalysts(){return mode==2?List.of(EmiIngredient.of(List.of(EmiStack.of(Blocks.TORCH),EmiStack.of(Blocks.CAMPFIRE)))):List.of();}
 public List<EmiStack> getOutputs(){return List.of(mode==0?EmiStack.of(Fluids.WATER,1000):mode==1?EmiStack.of(Items.CLAY):EmiStack.of(Fluids.LAVA,1000));}
 public boolean supportsRecipeTree(){return false;}public int getDisplayWidth(){return 170;}public int getDisplayHeight(){return 75;}
 public void addWidgets(WidgetHolder w){var in=getInputs();for(int i=0;i<in.size();i++)w.addSlot(in.get(i),4+i*20,5);w.addText(Component.literal(">"),74,10,0x404040,false);w.addSlot(getOutputs().getFirst(),112,5).recipeContext(this);w.addText(Component.translatable("recipe.revoindustry."+mode),4,32,0x404040,false);w.addText(Component.translatable(mode==2?"recipe.revoindustry.heat":"recipe.revoindustry.sky"),4,47,0x555555,false);w.addText(Component.translatable("recipe.revoindustry.bucket"),4,61,0x555555,false);}
 }
}

