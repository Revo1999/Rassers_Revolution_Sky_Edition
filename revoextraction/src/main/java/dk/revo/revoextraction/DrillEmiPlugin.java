package dk.revo.revoextraction;

import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public class DrillEmiPlugin implements EmiPlugin {
    static final EmiRecipeCategory CATEGORY=new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"wooden_drill_assembly"),EmiStack.of(RevoExtraction.CONTROLLER.get()));
    static final EmiRecipeCategory IRON_CATEGORY=new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"iron_drill_assembly"),EmiStack.of(RevoExtraction.IRON_CONTROLLER.get()));
    static final EmiRecipeCategory STEEL_CATEGORY=new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"steel_drill_assembly"),EmiStack.of(RevoExtraction.STEEL_CONTROLLER.get()));
    static final EmiRecipeCategory ELECTRIC_CATEGORY=new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"electric_drill_assembly"),EmiStack.of(RevoExtraction.ELECTRIC.get()));
    @Override public void register(EmiRegistry r){
        r.addRecipe(new EmiInfoRecipe(java.util.List.of(EmiStack.of(RevoExtraction.MINERAL_SLUDGE.get()),EmiStack.of(RevoExtraction.GEM_SLUDGE.get()),EmiStack.of(RevoExtraction.CRYSTALLINE_SLUDGE.get())),java.util.List.of(
            net.minecraft.network.chat.Component.literal("Iron and Steel Drills supply Mineral Sludge. Wash it with a water-backed Create Encased Fan for one Quartz."),
            net.minecraft.network.chat.Component.literal("Steel Drills supply Gem Sludge instead of loose gems. Fan washing yields one Diamond and two Lapis, with a 25% Emerald chance."),
            net.minecraft.network.chat.Component.literal("Electric Crystalline Sludge requires a retained Deep Survey Core, 128 EU/t and 1200 powered ticks. MI Centrifuge: 32 EU/t, 400 ticks, two Certus and two Quartz. Native MI quarry controller recipes are disabled.")),ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"info/drill_sludge")));
        r.addCategory(ELECTRIC_CATEGORY);r.addWorkstation(ELECTRIC_CATEGORY,EmiStack.of(RevoExtraction.ELECTRIC.get()));
        r.addRecipe(new MultiblockEmiRecipe(ELECTRIC_CATEGORY,ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"assembly/electric_drill"),DrillGuideDefinition.electric(),EmiStack.of(RevoExtraction.ELECTRIC.get())) {});
        r.addRecipe(new EmiInfoRecipe(java.util.List.of(EmiStack.of(RevoExtraction.DIGITAL.get()),EmiStack.of(RevoExtraction.DRY_SEDIMENT.get())),java.util.List.of(net.minecraft.network.chat.Component.literal("Digital Drill: 1x1, 512 EU/t. Open Sides and cycle each world face: Off, blue input, orange output, yellow EU. Coloured MI hatch overlays show the selected role. Defaults: top input, bottom output, south EU."),net.minecraft.network.chat.Component.literal("Geological programs co-produce dry sediment, without coolant or liquid output. Both items must fit before work starts. Retained survey cores still gate advanced programs. Native MI Mixer: one dry sediment + 1000 mB water -> 1000 mB Excellent Sediment Water, 32 EU/t, 200 base ticks. Glacial Ice produces no sediment.")),ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"info/digital_drill")));
        r.addCategory(STEEL_CATEGORY);r.addWorkstation(STEEL_CATEGORY,EmiStack.of(RevoExtraction.STEEL_CONTROLLER.get()));
        r.addRecipe(new dev.emi.emi.api.recipe.EmiInfoRecipe(java.util.List.of(dev.emi.emi.api.stack.EmiStack.of(RevoExtraction.ELECTRIC.get())),java.util.List.of(net.minecraft.network.chat.Component.literal("LV Electric Geological Drill: 16 EU/t, 200 powered ticks per resource. Select Mixed, Silver, Nickel, Lead, Bauxite or Antimony. Build the 3x3 multiblock shown in the construction recipe. Supply native MI energy, water and item input/output hatches in its top layer. Retained core and Glacial Ice bucket enter through item input."),net.minecraft.network.chat.Component.literal("Glacial Ice consumes one water bucket per ice, returning the empty bucket. Crystalline Sludge and Deep Survey require the retained Deep Survey Core; both cost 128 EU/t for 1200 ticks. Centrifuge Crystalline Sludge for Certus Quartz. Deep Survey, consumes 128 EU/t for 1200 ticks and yields one Ancient Debris every 64 completed results. 100 mB cooling water per geological cycle becomes Good Sediment Water at LV, Excellent at MV/HV. Full item/fluid output pauses work and coolant. Drain before changing grade. Mixer: Good + equal water -> double seawater; Excellent + triple water -> quadruple seawater. All programs and cycle counters persist.")),net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("revoextraction","electric_drill_guide")));
        r.addRecipe(new MultiblockEmiRecipe(STEEL_CATEGORY,ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"assembly/steel_drill"),DrillGuideDefinition.create(DrillTier.STEEL),EmiStack.of(RevoExtraction.STEEL_CONTROLLER.get())) {});
        r.addRecipe(new EmiInfoRecipe(java.util.List.of(EmiStack.of(RevoExtraction.STEEL_CONTROLLER.get()),EmiStack.of(RevoExtraction.STEEL_TANK.get()),EmiStack.of(RevoExtraction.REPAIR_KIT.get())),java.util.List.of(
            net.minecraft.network.chat.Component.literal("Steel Drill: 3 x 3 x 3. 12 supports, 8 placed casings, core, controller, shaft input, item hatch, side water tank and rear-top MI fluid output hatch. Keep the bottom centre empty."),
            net.minecraft.network.chat.Component.literal("64+ RPM; 16 SU/RPM (1024 SU at 64 RPM). 600 work/item. Water-only tank: 8000 mB. Consumes exactly 100 mB per completed cycle, distributed over its work; the same volume leaves as sediment-laden water."),
            net.minecraft.network.chat.Component.literal("Fill the left-middle tank with buckets or fluid pipes on any face. An MI water pump or Create fluid network can supply it. Rear-top sediment hatch: native MI tank/menu/auto-output, 8000 mB. Drain it into a Mixer for seawater. No kinetic-to-electric conversion."),
            net.minecraft.network.chat.Component.literal("Attempting to run dry stops production and seizes the drill after 5 seconds. Stopping the shaft pauses damage; adding water before seizure clears the warning. Full item OR fluid outputs pause work without consuming water or causing damage."),
            net.minecraft.network.chat.Component.literal("Repair: form the structure, STOP the shaft, supply at least 100 mB water, then right-click any machine part with a Drill Repair Kit. Output survives seizure and repair."),
            net.minecraft.network.chat.Component.literal("Regular 16-item cycle: 3 raw iron, 1 copper, 1 tin, 2 coal, 1 zinc, 1 gold, 1 redstone, 2 raw lead, 1 raw antimony, 1 Mineral Sludge and 2 Gem Sludge. Wash sludge with Create fans for gems; no direct gem output. Every 128th output is Flux Dust instead of its regular item. Auto-ejects at cycle end.")),ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"info/steel_drill")));

        r.addCategory(CATEGORY);r.addWorkstation(CATEGORY,EmiStack.of(RevoExtraction.CONTROLLER.get()));r.addRecipe(new Assembly());
        r.addCategory(IRON_CATEGORY);r.addWorkstation(IRON_CATEGORY,EmiStack.of(RevoExtraction.IRON_CONTROLLER.get()));
        r.addRecipe(new MultiblockEmiRecipe(IRON_CATEGORY,ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"assembly/iron_drill"),DrillGuideDefinition.create(DrillTier.IRON),EmiStack.of(RevoExtraction.IRON_CONTROLLER.get())) {});
        r.addRecipe(new EmiInfoRecipe(java.util.List.of(EmiStack.of(RevoExtraction.IRON_CONTROLLER.get())),java.util.List.of(
            net.minecraft.network.chat.Component.literal("Iron Drill: 3 x 3 x 3. Uses only iron-tier parts. Craft two cores: one for the controller and one to place."),
            net.minecraft.network.chat.Component.literal("Create input: at least 32 RPM, 8 SU/RPM. 600 rotational work per item (18.75 ticks at 32 RPM)."),
            net.minecraft.network.chat.Component.literal("Regular 13-item cycle: 3 raw iron, 2 raw copper, 2 raw tin, 2 coal, 1 raw zinc, 1 raw gold, 1 redstone and 1 Mineral Sludge. Wash Mineral Sludge for quartz. Every 128th output is Flux Dust instead of its regular item."),
            net.minecraft.network.chat.Component.literal("Outputs through the lower left hatch; auto-ejects forward at cycle end. Stops if output is full. Recipe view opens the interactive layer-by-layer build guide.")),ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"info/iron_drill")));
    }
    static MultiblockGuide guide(){return DrillGuideDefinition.create();}
    static class Assembly extends MultiblockEmiRecipe {
        Assembly(){super(CATEGORY,ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"assembly/wooden_drill"),guide(),EmiStack.of(RevoExtraction.CONTROLLER.get()));}
    }
}
