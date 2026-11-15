package dk.revo.revoindustry.client;

import dk.revo.revoindustry.hatchery.*;
import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

@EmiEntrypoint
public final class HatcheryEmiPlugin implements EmiPlugin {
    public static MultiblockGuide guide(){
        var parts=new ArrayList<MultiblockGuide.Part>();
        for(var c:HatcheryLayout.SHELL){var role=switch(c.part()){case CONTROLLER->MultiblockGuide.Role.CONTROLLER;case INPUT->MultiblockGuide.Role.ITEM_INPUT;case OUTPUT->MultiblockGuide.Role.OUTPUT;case ENERGY->MultiblockGuide.Role.ENERGY_INPUT;default->MultiblockGuide.Role.BLOCK;};parts.add(new MultiblockGuide.Part(c.offset(),HatcheryLayout.state(c,Direction.NORTH),role));}
        return new MultiblockGuide(Component.literal("Electric Wither Hatchery"),parts,List.of(
            Component.literal("5 x 5 base: front-centre controller, left-centre egg input, right-centre star output, rear-centre energy. Ports face outward."),
            Component.literal("Casing ring; three smoked-glass blocks in the front centre and a red hatching light at each front corner. Interior stays air."),
            Component.literal("Casing ring with three front glass blocks. Keep the 3 x 3 interior empty."),
            Component.literal("Repeat the glass row and red corner lights. The front window is 3 x 3; interior remains air."),
            Component.literal("Close the entire roof with containment casing. Supply 8192 EU/t or equivalent FE at the rear energy hatch.")),Component.literal("8192 EU/t | 75 seconds | 1 Wither Egg -> 1 Nether Star"));
    }
    public void register(EmiRegistry registry){
        var assembly=new EmiRecipeCategory(Hatchery.id("wither_assembly"),EmiStack.of(Hatchery.CONTROLLER.get()));registry.addCategory(assembly);registry.addWorkstation(assembly,EmiStack.of(Hatchery.CONTROLLER.get()));registry.addRecipe(new MultiblockEmiRecipe(assembly,Hatchery.id("assembly/wither_hatchery"),guide(),EmiStack.of(Hatchery.CONTROLLER.get())));
        var processing=new EmiRecipeCategory(Hatchery.id("wither_hatchery"),EmiStack.of(Hatchery.CONTROLLER.get()));registry.addCategory(processing);registry.addWorkstation(processing,EmiStack.of(Hatchery.CONTROLLER.get()));registry.addRecipe(new Process(processing));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Hatchery.EGG.get())),List.of(Component.literal("Build the vanilla Wither T: four soul sand or soul soil and three Wither Skeleton skulls. The complete structure disappears and drops one Wither Egg instead of summoning a roaming boss."),Component.literal("No player interaction is required. Block placers or dispensers may complete the ritual; a hopper below the centre collects the egg. Works with either horizontal orientation and with the final base block placed last.")),Hatchery.id("info/wither_egg")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Hatchery.CONTROLLER.get())),List.of(Component.literal("81 casings, 9 smoked-glass blocks, 4 red lights, a controller, item input/output hatches and an energy hatch. Leave the 3 x 3 x 3 interior clear."),Component.literal("One egg uses 12,288,000 EU over 1500 productive ticks: 60 seconds incubation, 5 seconds visible hatching and 10 seconds neutralization. Default FE cost: 122,880,000 FE."),Component.literal("The real contained Wither takes damage inside the chamber. Power loss, broken structure or full output pauses work without losing the egg. Breaking the controller returns the in-flight egg; spent electricity is not refunded.")),Hatchery.id("info/wither_hatchery")));
    }
    record Process(EmiRecipeCategory category) implements EmiRecipe {
        public EmiRecipeCategory getCategory(){return category;}public ResourceLocation getId(){return Hatchery.id("processing/wither_egg");}
        public List<EmiIngredient> getInputs(){return List.of(EmiStack.of(Hatchery.EGG.get()));}public List<EmiStack> getOutputs(){return List.of(EmiStack.of(Items.NETHER_STAR));}
        public int getDisplayWidth(){return 170;}public int getDisplayHeight(){return 65;}
        public void addWidgets(WidgetHolder w){w.addSlot(getInputs().getFirst(),8,8);w.addText(Component.literal("->"),70,13,0x404040,false);w.addSlot(getOutputs().getFirst(),132,8).recipeContext(this);w.addText(Component.literal("8192 EU/t | 75 seconds"),8,34,0x404040,false);w.addText(Component.literal("Hatch, contain, neutralize"),8,49,0x555555,false);}
    }
}
