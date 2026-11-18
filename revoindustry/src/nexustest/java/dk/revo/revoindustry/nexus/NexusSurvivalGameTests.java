package dk.revo.revoindustry.nexus;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.Simulation;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class NexusSurvivalGameTests {
 static void tickNative(GameTestHelper h,ElectricCraftingMachineBlockEntity machine){h.getLevel().getServer().getWorldData().overworldData().setGameTime(h.getLevel().getGameTime()+1);machine.tick();}
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));}
 static MachineRecipe recipe(GameTestHelper h,String name){return (MachineRecipe)h.getLevel().getRecipeManager().byKey(Nexus.id("nexus/"+name)).orElseThrow().value();}
 static long count(List<aztech.modern_industrialization.inventory.ConfigurableItemStack> slots,Item item){return slots.stream().filter(s->s.toStack().is(item)).mapToLong(s->s.getAmount()).sum();}
 static void feed(GameTestHelper h,MachineRecipe recipe,List<aztech.modern_industrialization.inventory.ConfigurableItemStack> items,List<aztech.modern_industrialization.inventory.ConfigurableFluidStack> fluids){
  h.assertTrue(items.size()>=recipe.itemInputs.size()&&fluids.size()>=recipe.fluidInputs.size(),"Actual native input slot capacity supports the production recipe");
  items.forEach(s->s.empty());fluids.forEach(s->s.empty());
  for(int i=0;i<recipe.itemInputs.size();i++){var in=recipe.itemInputs.get(i);var slot=items.get(i);slot.setKey(ItemVariant.of(in.getInputItems().getFirst()));slot.setAmount(in.amount());}
  for(int i=0;i<recipe.fluidInputs.size();i++){var in=recipe.fluidInputs.get(i);var slot=fluids.get(i);slot.setKey(FluidVariant.of(in.getInputFluids().getFirst()));slot.setAmount(in.amount());}
 }
 static void checkOutputs(GameTestHelper h,MachineRecipe recipe,List<aztech.modern_industrialization.inventory.ConfigurableItemStack> outputs){for(var o:recipe.itemOutputs)h.assertTrue(count(outputs,o.getStack().getItem())==o.amount(),"Exact registered production output "+o.getStack()+"; actual: "+outputs.stream().map(s->s.toStack().toString()).toList());}
 static void production(GameTestHelper h,NexusMachine machine,String name){var r=recipe(h,name);var inv=machine.getMultiblockInventoryComponent();inv.getItemOutputs().forEach(s->s.empty());inv.getFluidOutputs().forEach(s->s.empty());feed(h,r,inv.getItemInputs(),inv.getFluidInputs());for(int t=0;t<r.duration+50;t++){machine.getEnergyComponents().getFirst().insertEu(machine.getBaseRecipeEu(),Simulation.ACT);NexusGameTests.step(machine,1);}checkOutputs(h,r,inv.getItemOutputs());}
 static ElectricCraftingMachineBlockEntity nativeMachine(GameTestHelper h,String name){var p=h.absolutePos(new BlockPos(5,2,5));h.getLevel().setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(p,BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:"+name)).defaultBlockState());var m=(ElectricCraftingMachineBlockEntity)h.getLevel().getBlockEntity(p);m.components.getOrThrow(UpgradeComponent.class).setStackServer(m,new ItemStack(item("modern_industrialization:quantum_upgrade")));m.components.getOrThrow(CasingComponent.class).setCasingServer(m,new ItemStack(item("modern_industrialization:quantum_machine_hull")));h.assertTrue(m.getMaxRecipeEu()>=32768,"Native quantum upgrade enables endgame assembler EU");return m;}
 @GameTest(template="nexus_test",timeoutTicks=200)
 public static void castingMouldsAndAllThreePlatingStagesProduceRealMaterials(GameTestHelper h){
  var cast=NexusGameTests.build(h,"pressure_casting_plant",Direction.NORTH);for(String name:List.of("panel","light_panel","heavy_panel","casing_blank","carrier","frame","ring_blank","core_housing"))production(h,cast,"cast_"+name);NexusGameTests.clear(h,cast);
  var line=NexusGameTests.build(h,"electroplating_line",Direction.NORTH);for(String name:List.of("plating_clean","plating_deposit","plating_rinse"))production(h,line,name);NexusGameTests.clear(h,line);h.succeed();
 }
 @GameTest(template="nexus_test",timeoutTicks=200)
 public static void colliderTargetsWaitForBothInputsAndCompleteOnePaidReaction(GameTestHelper h){
  var m=NexusGameTests.build(h,"particle_collider",Direction.EAST);var r=recipe(h,"exotic_matter");var inv=m.getMultiblockInventoryComponent();feed(h,r,inv.getItemInputs(),inv.getFluidInputs());var right=inv.getItemInputs().get(2);right.empty();m.getEnergyComponents().getFirst().insertEu(1000000,Simulation.ACT);long power=m.getEnergyComponents().getFirst().getEu();NexusGameTests.step(m,40);h.assertTrue(m.completedReactions==0&&m.getEnergyComponents().getFirst().getEu()==power&&inv.getItemInputs().get(0).getAmount()==8,"Unbalanced target delivery is saved without spending energy or essence");
  right.setKey(ItemVariant.of(NexusMaterials.item("nexus_iridium_target")));right.setAmount(8);
  for(int t=0;t<r.duration+100;t++){m.getEnergyComponents().getFirst().insertEu(m.getBaseRecipeEu(),Simulation.ACT);NexusGameTests.step(m,1);}checkOutputs(h,r,inv.getItemOutputs());h.assertTrue(m.completedReactions==1,"One real collider batch triggers one completion flash");NexusGameTests.clear(h,m);h.succeed();
 }
 @GameTest(template="nexus_test",timeoutTicks=100)
 public static void coreAssemblyUsesSixSingularitiesAndProgramRetainsCompletedCore(GameTestHelper h){
  var ignition=recipe(h,"ignition_cell");h.assertTrue(ignition.itemInputs.stream().anyMatch(i->i.getInputItems().contains(item("modern_industrialization:singularity"))&&i.amount()==1),"One native singularity per Ignition Cell");
  var core=recipe(h,"core");h.assertTrue(core.itemInputs.stream().anyMatch(i->i.getInputItems().contains(NexusMaterials.item("nexus_ignition_cell"))&&i.amount()==6),"Core requires six completed cells");
  var m=nativeMachine(h,"assembler");var r=recipe(h,"construction_program");var slots=m.getInventory().getItemStacks();var inputs=slots.stream().filter(s->s.canPipesInsert()).toList();var outputs=slots.stream().filter(s->s.canPipesExtract()).toList();feed(h,r,inputs,m.getInventory().getFluidStacks());
  inputs.get(1).empty();for(int i=0;i<250;i++){m.getEnergyComponent().insertEu(32768,Simulation.ACT);tickNative(h,m);}h.assertTrue(count(outputs,Nexus.PROGRAM.get())==0,"A blank program cannot bypass the completed Core gate");inputs.get(1).setKey(ItemVariant.of(Nexus.block("nexus_core").asItem()));inputs.get(1).setAmount(1);
  for(int i=0;i<250;i++){m.getEnergyComponent().insertEu(32768,Simulation.ACT);tickNative(h,m);}h.assertTrue(count(outputs,Nexus.PROGRAM.get())==1&&count(inputs,Nexus.block("nexus_core").asItem())==1,"Actual native assembler verifies and retains the completed Core while finishing one program");h.succeed();
 }
 @GameTest(template="nexus_test",timeoutTicks=100)
 public static void nativeCentrifugeReturnsCellsBeforeSingleOutputLatticeAssembly(GameTestHelper h){for(String name:List.of("exotic_matter_decapsulation","phase_lattice")){var m=nativeMachine(h,name.equals("phase_lattice")?"assembler":"centrifuge");var r=recipe(h,name);var slots=m.getInventory().getItemStacks();var inputs=slots.stream().filter(s->s.canPipesInsert()).toList();var outputs=slots.stream().filter(s->s.canPipesExtract()).toList();feed(h,r,inputs,m.getInventory().getFluidStacks());for(int i=0;i<r.duration+80;i++){m.getEnergyComponent().insertEu(32768,Simulation.ACT);tickNative(h,m);}checkOutputs(h,r,outputs);}h.succeed();}
 @GameTest(template="nexus_test",timeoutTicks=100)
 public static void everyNexusBlockAndMaterialHasAProductionRecipe(GameTestHelper h){
  var outputs=new HashSet<Item>();for(var holder:h.getLevel().getRecipeManager().getRecipes())if(holder.value() instanceof MachineRecipe r)for(var out:r.itemOutputs)outputs.add(out.getStack().getItem());
  for(var block:Nexus.BLOCKS.values())h.assertTrue(outputs.contains(block.get().asItem()),"Survival recipe for every placed Nexus block: "+block.getId());for(var material:NexusMaterials.ITEMS.entrySet())h.assertTrue(outputs.contains(material.getValue().get()),"No orphan material: "+material.getKey());
  h.assertTrue(h.getLevel().getServer().getAdvancements().get(Nexus.id("nexus_stabilized"))!=null,"Server-verifiable final stabilization advancement exists");h.succeed();
 }

 @GameTest(template="nexus_test",timeoutTicks=200)
 public static void allProductionAssemblerRecipesAreSelectable(GameTestHelper h){var names=new ArrayList<String>();for(var holder:h.getLevel().getRecipeManager().getRecipes())if(holder.id().getNamespace().equals("revoindustry")&&holder.id().getPath().startsWith("nexus/")&&holder.value() instanceof MachineRecipe r&&r.getType()==aztech.modern_industrialization.machines.init.MIMachineRecipeTypes.ASSEMBLER)names.add(holder.id().getPath().substring(6));for(String name:names){var m=nativeMachine(h,"assembler");var r=recipe(h,name);var slots=m.getInventory().getItemStacks();feed(h,r,slots.stream().filter(s->s.canPipesInsert()).toList(),m.getInventory().getFluidStacks());m.getCrafterComponent().lockRecipe(Nexus.id("nexus/"+name),h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL).getInventory());for(int t=0;t<r.duration+80;t++){m.getEnergyComponent().insertEu(32768,Simulation.ACT);tickNative(h,m);}if(count(slots,r.itemOutputs.getFirst().getStack().getItem())==0)throw new IllegalStateException(name+" progress="+m.getCrafterComponent().getProgress()+" energy="+m.getEnergyComponent().getEu()+" max="+m.getMaxRecipeEu()+" multiple="+m.getCrafterComponent().matchesMultipleRecipes()+" slots="+slots.stream().map(v->v.toStack().toString()).toList());checkOutputs(h,r,slots.stream().filter(s->s.canPipesExtract()).toList());}h.succeed();}
 @GameTest(template="nexus_test",timeoutTicks=100)
 public static void builderGhostCounterDepositsWithdrawsAndDropsExactlyOnce(GameTestHelper h){var builder=NexusGameTests.build(h,"nexus_builder",Direction.NORTH);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setPos(builder.getBlockPos().getCenter());var menu=new NexusBuilderMenu(1,player.getInventory(),builder);int index=0;for(int i=0;i<NexusConstruction.requirements().size();i++)if(NexusConstruction.requirements().get(i).amount()>64){index=i;break;}var need=NexusConstruction.requirements().get(index);for(int i=0;i<10;i++){menu.setCarried(new ItemStack(need.item(),64));menu.clickMenuButton(player,200+index);}h.assertTrue(builder.construction.stored(index)==need.amount(),"Ghost counter caps at the full required quantity above64");menu.setCarried(ItemStack.EMPTY);menu.clickMenuButton(player,200+index);h.assertTrue(menu.getCarried().getCount()==64&&builder.construction.stored(index)==need.amount()-64,"Click withdraws at most64");menu.setCarried(ItemStack.EMPTY);var drops=builder.dropExtra();h.assertTrue(drops.stream().filter(s->s.is(need.item())).mapToInt(ItemStack::getCount).sum()==need.amount()-64&&builder.dropExtra().stream().noneMatch(s->s.is(need.item())),"Breaking returns the remaining stored material exactly once");NexusGameTests.clear(h,builder);h.succeed();}
}
