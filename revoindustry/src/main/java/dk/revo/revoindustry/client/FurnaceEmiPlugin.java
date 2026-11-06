package dk.revo.revoindustry.client;
import dk.revo.revoindustry.furnace.*;
import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
@EmiEntrypoint
public final class FurnaceEmiPlugin implements EmiPlugin {
 public static MultiblockGuide guide(FurnaceTier tier){
  var controller=(FurnaceController)Furnaces.CONTROLLERS.get(tier).get().getBlockEntityInstance();var shape=controller.getActiveShape();var parts=new ArrayList<MultiblockGuide.Part>();
  String prefix=new String[]{"bronze","steel","advanced"}[tier.factoryTier];
  for(var entry:shape.simpleMembers.entrySet()){
   var p=entry.getKey();var state=entry.getValue().getPreviewState();var role=p.equals(new BlockPos(0,2,1))?MultiblockGuide.Role.VENT:MultiblockGuide.Role.BLOCK;
   if(p.equals(new BlockPos(-1,0,0))){state=Furnaces.ITEM_HATCHES.get(tier.factoryTier*2).get().defaultBlockState();role=MultiblockGuide.Role.ITEM_INPUT;}
   if(p.equals(new BlockPos(1,0,0))){state=Furnaces.ITEM_HATCHES.get(tier.factoryTier*2+1).get().defaultBlockState();role=MultiblockGuide.Role.OUTPUT;}
   if(p.equals(new BlockPos(0,0,2))){state=Furnaces.FUEL_HATCHES.get(tier.factoryTier*3).get().defaultBlockState();role=MultiblockGuide.Role.FUEL_INPUT;}
   var data=net.neoforged.neoforge.client.model.data.ModelData.EMPTY;
   if(role!=MultiblockGuide.Role.BLOCK){var appearance=new aztech.modern_industrialization.machines.models.MachineModelClientData(tier.casing());data=net.neoforged.neoforge.client.model.data.ModelData.builder().with(aztech.modern_industrialization.machines.models.MachineModelClientData.KEY,appearance).build();}
   parts.add(new MultiblockGuide.Part(p,state,role,data));
  }
  var controllerAppearance=new aztech.modern_industrialization.machines.models.MachineModelClientData(tier.casing(),Direction.NORTH);
  parts.add(new MultiblockGuide.Part(BlockPos.ZERO,Furnaces.CONTROLLERS.get(tier).get().defaultBlockState(),MultiblockGuide.Role.CONTROLLER,net.neoforged.neoforge.client.model.data.ModelData.builder().with(aztech.modern_industrialization.machines.models.MachineModelClientData.KEY,controllerAppearance).build()));
  return new MultiblockGuide(Component.literal(tier.id.toUpperCase(Locale.ROOT)+" Multiblock Furnace"),parts,List.of(Component.literal("Bottom: front controller, item input/output hatches, and a fuel hatch. Other shell positions also accept hatches."),Component.literal("Middle: matching casing ring. Leave the single centre block empty."),Component.literal("Top: matching casing, with the adapting exhaust vent in the exact centre.")),Component.literal("Solid, liquid or gas fuel; three throughput grades. Minimum item hatch grade: "+(tier.factoryTier+1)));
 }
 public void register(EmiRegistry registry){
  var category=new EmiRecipeCategory(Furnaces.id("furnace_assembly"),EmiStack.of(Furnaces.CONTROLLERS.get(FurnaceTier.IRON).get()));registry.addCategory(category);
  registry.removeEmiStacks(stack->stack.getId().equals(ResourceLocation.parse("ironfurnaces:augment_factory")));
  for(var tier:FurnaceTier.values()){
   var stack=EmiStack.of(Furnaces.CONTROLLERS.get(tier).get());registry.addWorkstation(category,stack);registry.addRecipe(new MultiblockEmiRecipe(category,Furnaces.id("assembly/"+tier.id+"_furnace"),guide(tier),stack));
   registry.addRecipe(new EmiInfoRecipe(List.of(stack),List.of(Component.literal("3 x 3 x 3 hollow furnace: 21 casings, controller, roof-centre vent, and at least one item input, item output and fuel hatch. Extra hatches may replace shell casings. Native MI hatches adapt to the formed casing."),Component.literal("Permanent Factory processing: "+tier.lanes()+" parallel smelting lanes, batch size up to "+tier.batch()+" per lane. Furnace speed follows Iron Furnaces' configuration. All vanilla/modded SMELTING recipes are supported; this is not an ore multiplier."),Component.literal("Use Revo item hatches (1 / 2 / 4 slots) or matching Bronze / Steel / Advanced MI hatches. Minimum grade "+(tier.factoryTier+1)+" for this furnace. Solid, Liquid and Gas fuel inputs each have three grades: 64 / 512 / 4096 heat per tick; fluid tanks 4 / 16 / 64 buckets. Fuel inputs do not accept FE or EU."),Component.literal("Solid fuels use vanilla furnace burn time; lava supplies 20 heat/mB. Other liquid/gas fuels use MI's fuel data map, at 2 EU per heat. Gas is identified by the c:gaseous fluid tag. Water, steam and nonfuel fluids are rejected."),Component.literal("Blocked output or an incomplete structure pauses work without consuming more fuel. In-progress ingredients survive saves; breaking the controller returns them. Open the controller to claim stored smelting experience.")),Furnaces.id("info/"+tier.id+"_furnace")));
  }
 }
}
