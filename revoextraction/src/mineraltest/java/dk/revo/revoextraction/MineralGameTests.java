package dk.revo.revoextraction;

import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public final class MineralGameTests {
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));}
 static int count(net.neoforged.neoforge.items.IItemHandler inv, Item item){int n=0;for(int s=0;s<inv.getSlots();s++)if(inv.getStackInSlot(s).is(item))n+=inv.getStackInSlot(s).getCount();return n;}
 static ElectricDrillEntity electric(GameTestHelper h){var p=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(p,RevoExtraction.ELECTRIC.get().defaultBlockState());return (ElectricDrillEntity)h.getLevel().getBlockEntity(p);}
 static void step(ElectricDrillEntity e,int ticks){for(int i=0;i<ticks;i++){e.receive(e.cost(),false);ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);}}

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void mechanicalSteelSuppliesBatteriesAndSludge(GameTestHelper h){
  var l=h.getLevel();var p=h.absolutePos(new BlockPos(5,2,5));var tier=DrillTier.STEEL;
  l.setBlockAndUpdate(p,RevoExtraction.STEEL_CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,Direction.NORTH));var m=(DrillMachine)l.getBlockEntity(p);
  for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
   var part=tier.part(x,y,z);var s=tier.block(part).defaultBlockState();
   if(part==DrillLayout.Part.CONTROLLER)s=s.setValue(DrillController.FACING,Direction.NORTH);
   if(part==DrillLayout.Part.INPUT)s=s.setValue(DrillInput.FACING,Direction.EAST);
   if(part==DrillLayout.Part.HATCH)s=s.setValue(DrillHatch.FACING,Direction.WEST);
   l.setBlockAndUpdate(m.at(x,y,z),s);
  }
  h.assertTrue(m.checkStructure(),"Real mechanical steel drill forms");
  ((DrillWaterTankEntity)l.getBlockEntity(m.at(-1,1,0))).water.fill(new FluidStack(Fluids.WATER,8000),FluidAction.EXECUTE);
  ((DrillDrive)l.getBlockEntity(m.at(1,1,0))).setSpeed(256);
  var chestPos=m.at(-1,0,0).west();l.setBlockAndUpdate(chestPos,Blocks.CHEST.defaultBlockState());var out=l.getCapability(Capabilities.ItemHandler.BLOCK,chestPos,Direction.EAST);
  for(int i=0;i<100&&m.getPersistentData().getInt("cycle")<16;i++)DrillMachine.tick(l,p,m.getBlockState(),m);
  h.assertTrue(m.getPersistentData().getInt("cycle")==16,"Completed one saved 16-output cycle");
  h.assertTrue(count(out,item("modern_industrialization:raw_lead"))==2&&count(out,item("modern_industrialization:raw_antimony"))==1,"Pre-electric lead and antimony reach output inventory");
  h.assertTrue(count(out,RevoExtraction.GEM_SLUDGE.get())==2&&count(out,RevoExtraction.MINERAL_SLUDGE.get())==1,"Sludge is automatically exported");
  h.assertTrue(count(out,Items.DIAMOND)==0&&count(out,Items.LAPIS_LAZULI)==0,"Drill never directly produces gems");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeCreateWashesSludgeIntoGems(GameTestHelper h){
  for(String name:new String[]{"mineral_sludge","gem_sludge"}){
   var recipe=(ProcessingRecipe<?, ?>)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("revoextraction:splashing/"+name)).orElseThrow().value();
   h.assertTrue(recipe.getType()==com.simibubi.create.AllRecipeTypes.SPLASHING.getType(),"Native fan washing type");
   h.assertTrue(recipe.getIngredients().getFirst().test(new ItemStack(item("revoextraction:"+name))),"Registered sludge input");
   var outputs=recipe.rollResults(net.minecraft.util.RandomSource.create(42));
   h.assertTrue(outputs.stream().anyMatch(s->s.is(name.equals("gem_sludge")?Items.DIAMOND:Items.QUARTZ)),"Actual native roll always supplies first required gem");
   if(name.equals("gem_sludge"))h.assertTrue(recipe.getRollableResults().stream().anyMatch(o->o.getStack().is(Items.EMERALD)&&o.getChance()==.25f),"Emerald byproduct balanced at 25 percent");
  }
  for(int i=0;i<384;i++)for(var tier:DrillTier.values())h.assertTrue(tier.output(i)!=Items.DIAMOND&&tier.output(i)!=Items.LAPIS_LAZULI,"No direct mechanical gem bypass");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void electricAntimonyAndSavedPrograms(GameTestHelper h){
  var e=electric(h);e.program=7;step(e,200);h.assertTrue(count(e.items,item("modern_industrialization:raw_antimony"))==1&&e.energy==0,"Antimony costs exactly 3200 EU");
  var tag=e.saveWithoutMetadata(h.getLevel().registryAccess());e.loadWithComponents(tag,h.getLevel().registryAccess());h.assertTrue(e.program==7&&e.cycle==1,"Appended program persists");
  for(int i=2;i<11;i++)e.items.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));e.energy=100;int cycle=e.cycle;ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.energy==100&&e.cycle==cycle&&e.work==0,"Full output consumes no work or power");
  h.assertTrue(ElectricDrillEntity.PROGRAMS[5].equals("Glacial ice")&&ElectricDrillEntity.PROGRAMS[6].equals("Deep survey"),"Existing save indices preserved");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void advancedSludgeRequiresCoreAndPower(GameTestHelper h){
  var e=electric(h);e.program=8;e.energy=128;ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.work==0&&e.energy==128,"No missing-core bypass");
  e.items.setStackInSlot(0,new ItemStack(RevoExtraction.SURVEY_CORE.get()));e.energy=0;step(e,1199);h.assertTrue(count(e.items,RevoExtraction.CRYSTALLINE_SLUDGE.get())==0,"No early sludge output");
  var saved=e.saveWithoutMetadata(h.getLevel().registryAccess());e.loadWithComponents(saved,h.getLevel().registryAccess());h.assertTrue(e.program==8&&e.work==1199,"Save resumes gated sludge without losing work");step(e,1);
  h.assertTrue(e.energy==0&&e.work==0&&count(e.items,RevoExtraction.CRYSTALLINE_SLUDGE.get())==1&&e.items.getStackInSlot(0).is(RevoExtraction.SURVEY_CORE.get()),"Full cycle costs 153600 EU and retains core");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void actualMiCentrifugeRecoversCertus(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(3,2,3));var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:centrifuge"));h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState());
  var machine=(ElectricCraftingMachineBlockEntity)h.getLevel().getBlockEntity(pos);var input=machine.getInventory().getItemStacks().getFirst();input.setKey(ItemVariant.of(RevoExtraction.CRYSTALLINE_SLUDGE.get()));input.setAmount(1);
  for(int t=0;t<500;t++){machine.getEnergyComponent().insertEu(32,Simulation.ACT);machine.tick();}
  long certus=machine.getInventory().getItemStacks().stream().filter(s->s.toStack().is(item("ae2:certus_quartz_crystal"))).mapToLong(s->s.getAmount()).sum();
  h.assertTrue(certus==2&&input.getAmount()==0,"Actual MI Centrifuge consumes sludge and produces two Certus");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeFirstBatteriesAndHullCraftWithoutAssembler(GameTestHelper h){
  var l=h.getLevel();
  for(String target:new String[]{"redstone_battery","basic_machine_hull"}){
   String[] cells=target.equals("redstone_battery")?
    new String[]{"tin_cable","battery_alloy_plate","tin_cable","battery_alloy_curved_plate","minecraft:redstone","battery_alloy_curved_plate","battery_alloy_curved_plate","minecraft:redstone","battery_alloy_curved_plate"}:
    new String[]{"minecraft:air","analog_circuit","minecraft:air","redstone_battery","steel_machine_casing","redstone_battery","tin_cable","tin_cable","tin_cable"};
   var stacks=java.util.Arrays.stream(cells).map(s->new ItemStack(item(s.contains(":")?s:"modern_industrialization:"+s))).toList();
   var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,stacks);
   var recipe=l.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,l).orElseThrow();
   h.assertTrue(recipe.value().assemble(input,l.registryAccess()).is(item("modern_industrialization:"+target)),"Actual crafting-table recipe yields "+target);
  }
  var alloy=l.getRecipeManager().byKey(ResourceLocation.parse("modern_industrialization:alloy/mixer/battery_alloy/dust")).orElseThrow().value();
  h.assertTrue(alloy.getIngredients().stream().anyMatch(i->i.test(new ItemStack(item("modern_industrialization:lead_dust"))))&&alloy.getIngredients().stream().anyMatch(i->i.test(new ItemStack(item("modern_industrialization:antimony_dust")))),"Native battery alloy accepts both newly reachable metals");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeQuarryControllersCannotBeCrafted(GameTestHelper h){
  var recipes=h.getLevel().getRecipeManager();
  for(String id:new String[]{"steam_age/steel/quarry_asbl","electric_age/machine/electric_quarry_asbl","assembler_generated/steam_age/steel/quarry","assembler_generated/electric_age/machine/electric_quarry"})h.assertTrue(recipes.byKey(ResourceLocation.parse("modern_industrialization:"+id)).isEmpty(),"Quarry controller crafting/assembler disabled: "+id);
  h.succeed();
 }
}
