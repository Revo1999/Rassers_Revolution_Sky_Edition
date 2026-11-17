package dk.revo.revoindustry.client;
import java.util.*;
import dk.revo.revoindustry.saltern.*;
import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
@EmiEntrypoint
public final class SalternEmiPlugin implements EmiPlugin {
    public static MultiblockGuide guide(){
        var parts=new ArrayList<MultiblockGuide.Part>();for(var c:SalternLayout.cells()){
            var role=switch(c.role()){case "controller"->MultiblockGuide.Role.CONTROLLER;case "water"->MultiblockGuide.Role.FLUID_INPUT;case "brine"->MultiblockGuide.Role.OUTPUT;case "energy"->MultiblockGuide.Role.ENERGY_INPUT;default->MultiblockGuide.Role.BLOCK;};parts.add(new MultiblockGuide.Part(c.offset(),SalternLayout.state(c,Direction.NORTH),role));
        }
        return new MultiblockGuide(Component.literal("Salt Geode Chamber"),parts,List.of(Component.literal("3 x 3 base: front controller, centre saline geode, left water input, right brine output, rear energy hatch; four corner casings."),Component.literal("Four steel corner pillars and four smoked-glass windows. Leave the centre air for the submerged geode and buds."),Component.literal("Close the 3 x 3 roof with containment casings. Supply water and 16 EU/t. Native MI Centrifuge converts the salt water to salt.")),Component.literal("16 EU/t | 20 seconds | 1000 mB water -> 1000 mB salt water"));
    }
    public void register(EmiRegistry registry){
        var assembly=new EmiRecipeCategory(Saltern.id("salt_geode_assembly"),EmiStack.of(Saltern.CONTROLLER.get()));registry.addCategory(assembly);registry.addWorkstation(assembly,EmiStack.of(Saltern.CONTROLLER.get()));registry.addRecipe(new MultiblockEmiRecipe(assembly,Saltern.id("assembly/salt_geode"),guide(),EmiStack.of(Saltern.CONTROLLER.get())));
        var growth=new EmiRecipeCategory(Saltern.id("salt_geode_growth"),EmiStack.of(Saltern.CORE.get()));registry.addCategory(growth);registry.addWorkstation(growth,EmiStack.of(Saltern.CONTROLLER.get()));registry.addRecipe(new Growth(growth));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Saltern.CONTROLLER.get())),List.of(Component.literal("3 x 3 x 3: 17 containment casings, 4 smoked-glass windows, controller, saline geode core, water input, brine output and energy hatch. All three ports face outward."),Component.literal("Growing buds are harvested under animated water and electrical arcs. Each bucket of water produces a bucket of salt water after 400 productive ticks at 16 EU/t. Full output, missing water/power or a broken structure pauses work safely."),Component.literal("Centrifuge 1000 mB salt water at 8 EU/t for 200 ticks to obtain 2 MI Salt Dust. This feeds native salt electrolysis. No salt is needed in the first turbine, chamber or centrifuge recipes, so no manual bootstrap is required.")),Saltern.id("info/salt_geode")));
    }
    record Growth(EmiRecipeCategory category) implements EmiRecipe {
        public EmiRecipeCategory getCategory(){return category;}public ResourceLocation getId(){return Saltern.id("processing/salt_geode");}
        public List<EmiIngredient> getInputs(){return List.of(EmiStack.of(Fluids.WATER,1000));}public List<EmiStack> getOutputs(){return List.of(EmiStack.of(Saltern.SALT_WATER.get(),1000));}public int getDisplayWidth(){return 170;}public int getDisplayHeight(){return 60;}
        public void addWidgets(WidgetHolder w){w.addSlot(getInputs().getFirst(),8,8);w.addText(Component.literal("->"),74,14,0x404040,false);w.addSlot(getOutputs().getFirst(),132,8).recipeContext(this);w.addText(Component.literal("16 EU/t | 20 s | seed retained"),8,38,0x404040,false);}
    }
}
