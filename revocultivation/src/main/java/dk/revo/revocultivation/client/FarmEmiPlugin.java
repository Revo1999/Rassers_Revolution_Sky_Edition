package dk.revo.revocultivation.client;
import java.util.*;
import dk.revo.revocultivation.*;
import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
@EmiEntrypoint
public class FarmEmiPlugin implements EmiPlugin {
    private static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,name);}
    private static final Map<MachineKind,EmiRecipeCategory> ASSEMBLY_CATEGORIES=new EnumMap<>(MachineKind.class);
    private static EmiRecipeCategory category(MachineKind kind){return ASSEMBLY_CATEGORIES.computeIfAbsent(kind,k->new EmiRecipeCategory(id(k.id+"_assembly"),EmiStack.of(k.controller())));}
    public static MultiblockGuide guide(MachineKind kind){return FarmGuideDefinition.create(kind);}
    public static MultiblockEmiRecipe assembly(MachineKind kind){return new MultiblockEmiRecipe(category(kind),id("assembly/"+kind.id),guide(kind),EmiStack.of(kind.controller()));}
    public static void open(MachineKind kind){EmiApi.displayRecipe(assembly(kind));}
    @Override public void register(EmiRegistry registry){
        SmallMachineEmi.register(registry);
        for(var kind:MachineKind.values())if(kind.registered()){
            var category=category(kind);registry.addCategory(category);registry.addWorkstation(category,EmiStack.of(kind.controller()));registry.addRecipe(assembly(kind));
            var production=new EmiRecipeCategory(id(kind.id+"_production"),EmiStack.of(kind.controller()));registry.addCategory(production);registry.addWorkstation(production,EmiStack.of(kind.controller()));
            int[] programs=kind.isTree()?new int[]{0}:kind.isGreenhouse()?new int[]{10,11,12,13,14,15,16}:kind.harvesterPrograms();
            for(int program:programs)registry.addRecipe(new Production(production,kind,program));
        }
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(MachineKind.HARVESTER.controller()),EmiStack.of(MachineKind.STEEL_HARVESTER.controller())),List.of(
            Component.literal("Right-click the species filter to select the contained mob. Wooden: Zombie, Skeleton, Spider, Cow, Slime. Steel: Zombie, Skeleton, Spider, Cow, Blaze, Slime, Enderman, Magma Cube."),
            Component.literal("Cow produces 1 leather and 2 raw beef per harvest: 15 seconds in wood, 7.5 seconds in steel. Leather is available before Steel Drill maintenance."),
            Component.literal("Steel Blaze mode produces 1 blaze rod every 7.5 seconds. The contained creature is retained and never becomes a roaming world entity.")),id("info/harvester_species")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(MachineKind.IRON_GREENHOUSE.controller())),List.of(
            Component.literal("Iron Greenhouse: 5 x 5 x 3 structure enclosing a full 3 x 3 crop bed. Square steel beams enclose connected glass panes with thin white LED strips under the top frame. A Basic Power Converter replaces the rear-right bottom support; the right-facing hatch sits immediately in front of it."),
            Component.literal("Wrench the converter so its FE face points to the REAR, with its EU face directly touching the iron I/O hatch. Set FE to EU mode. The formed machine supplies a fitted housing; the converter remains functional and stores zero power."),
            Component.literal("Supply "+8*dk.revo.revolib.energy.EnergyInterop.fePerEu()+" FE/t to the converter, or 8 EU/t directly to the hatch. Ratio follows MI's configuration. The machine has its own 1600 EU buffer; no kinetic input."),
            Component.literal("A retained wheat/carrot/potato/beetroot starter and 250 mB water produce one harvest every 10 s. Water capacity: 16000 mB. Automatic grow lights enable night and indoor production."),
            Component.literal("Fit the Iron Water Tank in the left-middle bottom position. Fill it by bucket or pipe. Its visible vanilla water level, controller and Jade all use the same saved tank. Crop models follow the actual selected species and growth stage."),
            Component.literal("Missing power, water, starter or output space pauses production. LED strips follow world time and rain: on from tick 13000 to 23000 or while raining, off during clear daytime. Idle lighting consumes 1 EU/t; active production remains 8 EU/t total. Controller: diagnostics only. Hatch: storage, water and native EU input.")),id("info/iron_greenhouse")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(MachineKind.STEEL_TREE.controller())),List.of(
            Component.literal("Steel Tree Grower: 5 x 5 x 1 base. 4 steel supports, 9 casings, 8 soil beds, 1 dirt block, controller, item hatch and attached steel water tank."),
            Component.literal("Keep a 5 x 5 x 7 growing volume above the base clear, with daylight above the canopy. Insert an oak sapling and supply water; 16000 mB capacity and 500 mB per cycle, charged when growth begins."),
            Component.literal("A real tree of six vanilla oak logs and persistent leaves appears halfway through the 20-second cycle. The machine saves which blocks it placed and harvests only those. Manual changes cancel that harvest to prevent duplicate output."),
            Component.literal("Water amount appears in the controller, hatch and Jade. Bucket or pipe water into the attached tank. Item output auto-ejects through the right-centre hatch.")),id("info/steel_tree_grower")));
    }
    private record Production(EmiRecipeCategory category,MachineKind kind,int program) implements EmiRecipe {
        public EmiRecipeCategory getCategory(){return category;}
        public ResourceLocation getId(){return id("production/"+kind.id+"/"+MachineKind.programName(program));}
        public List<EmiIngredient> getInputs(){List<EmiIngredient> list=new ArrayList<>();var seed=MachineKind.starter(program);if(!seed.isEmpty())list.add(EmiStack.of(seed));if(kind.needsWater())list.add(EmiStack.of(Fluids.WATER,kind.waterPerCycle()));if(kind.isHarvester())list.add(EmiStack.of(kind.block(MachineKind.Part.FILTER)));return list;}
        public List<EmiStack> getOutputs(){return MachineKind.products(program).stream().map(EmiStack::of).toList();}
        public int getDisplayWidth(){return 176;}public int getDisplayHeight(){return 72;}public boolean supportsRecipeTree(){return false;}
        public void addWidgets(WidgetHolder w){
            w.addText(Component.translatable("program.revocultivation."+MachineKind.programName(program)),4,2,0x404040,false);
            var inputs=getInputs();for(int i=0;i<inputs.size();i++)w.addSlot(inputs.get(i),4+i*18,19);
            w.addText(Component.literal(">"),52,24,0x404040,false);var outputs=getOutputs();for(int i=0;i<outputs.size();i++)w.addSlot(outputs.get(i),66+i*18,19);
            w.addText(Component.translatable("recipe.revocultivation.time",MachineKind.seconds(kind.ticks)),4,42,0x404040,false);
            w.addText(kind.electric()?Component.literal("8 EU/t / "+8*dk.revo.revolib.energy.EnergyInterop.fePerEu()+" FE/t | starter kept"):Component.translatable(kind.isHarvester()?"recipe.revocultivation.filter":"recipe.revocultivation.retained"),4,56,0x555555,false);
        }
    }
}
