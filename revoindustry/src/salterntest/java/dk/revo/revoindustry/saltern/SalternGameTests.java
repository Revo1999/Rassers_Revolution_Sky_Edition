package dk.revo.revoindustry.saltern;

import dk.revo.revoindustry.hatchery.*;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class SalternGameTests {
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void nativeMixerLeachesCoolantIntoSeawater(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(2,2,2));var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:electric_mixer"));h.assertTrue(block!=Blocks.AIR,"Native electric Mixer exists");h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState());var mixer=(ElectricCraftingMachineBlockEntity)h.getLevel().getBlockEntity(pos);
  var fluids=mixer.getInventory().fluidStorage.fluidHandler;h.assertTrue(fluids.fill(new FluidStack(Saltern.SEDIMENT.get(),1000),FluidAction.EXECUTE)==1000,"Actual Mixer accepts drill sediment fluid");
  for(int i=0;i<400;i++){mixer.getEnergyComponent().insertEu(32,Simulation.ACT);mixer.tick();}
  var sea=fluids.drain(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE);h.assertTrue(sea.getAmount()==1000,"1000 mB drill coolant yields exactly 1000 mB seawater without starting salt");
  var c=build(h,Direction.NORTH);h.assertTrue(c.water.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0,"Fresh water no longer bypasses sediment processing");h.assertTrue(c.water.fill(new FluidStack(Saltern.SEDIMENT.get(),1000),FluidAction.EXECUTE)==0,"Raw sediment must first be leached");h.assertTrue(c.water.fill(sea,FluidAction.EXECUTE)==1000,"Chamber accepts processed seawater");step(c,400,true);h.assertTrue(c.brine.getFluidAmount()==1000,"Seawater concentrates into salt water");h.succeed();
 }
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void legacyWaterAndAnimationPhasesAreStable(GameTestHelper h){
  var c=build(h,Direction.NORTH);var tag=c.saveWithoutMetadata(h.getLevel().registryAccess());var old=new net.neoforged.neoforge.fluids.capability.templates.FluidTank(8000);old.fill(new FluidStack(Fluids.WATER,2000),FluidAction.EXECUTE);tag.put("Water",old.writeToNBT(h.getLevel().registryAccess(),new net.minecraft.nbt.CompoundTag()));c.loadWithComponents(tag,h.getLevel().registryAccess());h.assertTrue(c.water.getFluid().is(Saltern.SEA_WATER.get())&&c.water.getFluidAmount()==2000,"Existing buffered water upgrades once without losing volume");
  for(int bud=0;bud<5;bud++)for(int tick=0;tick<400;tick++){float size=SalternAnimation.height(tick,bud);h.assertTrue(Float.isFinite(size)&&size>0&&size<=1,"Every growth/harvest pose stays valid");}
  h.assertTrue(SalternAnimation.growth(40,0)>SalternAnimation.growth(40,4),"Buds grow in sequence");h.assertTrue(SalternAnimation.harvest(350,0)>SalternAnimation.harvest(350,4),"Buds harvest in sequence");h.succeed();
 }
 static SalternController build(GameTestHelper h,Direction front){var l=h.getLevel();var origin=h.absolutePos(new BlockPos(5,2,5));for(int x=0;x<11;x++)for(int y=1;y<7;y++)for(int z=0;z<11;z++)l.setBlockAndUpdate(h.absolutePos(new BlockPos(x,y,z)),Blocks.AIR.defaultBlockState());for(var cell:SalternLayout.cells())l.setBlockAndUpdate(SalternLayout.at(origin,front,cell.offset()),SalternLayout.state(cell,front));return (SalternController)l.getBlockEntity(origin);}
 static void step(SalternController c,int count,boolean power){for(int i=0;i<count;i++){c.getLevel().getServer().getWorldData().overworldData().setGameTime(c.getLevel().getGameTime()+1);if(power)c.receiveEu(16,false);c.tick();}}
 static SalternPort port(SalternController c,int x,int z){return (SalternPort)c.getLevel().getBlockEntity(c.at(x,0,z));}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void allRotationsAndDirectionalPorts(GameTestHelper h){for(var front:Direction.Plane.HORIZONTAL){var c=build(h,front);h.assertTrue(c.validate(),"Chamber forms "+front+": "+c.problem());var input=port(c,-1,1);var output=port(c,1,1);h.assertTrue(input.fluids(input.facing().getOpposite())==null,"Only outward fluid face connects");var cached=input.fluids(input.facing());h.assertTrue(cached.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE)==0,"Water input rejects lava");h.assertTrue(output.fluids(null).fill(new FluidStack(Saltern.SALT_WATER.get(),1000),FluidAction.EXECUTE)==0,"Brine output rejects insertion");h.getLevel().setBlockAndUpdate(c.at(0,1,0),Blocks.AIR.defaultBlockState());h.assertTrue(!c.validate()&&cached.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE)==0,"Cached capability refuses broken structure");}h.succeed();}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void exactWaterEnergyAndOutputBalance(GameTestHelper h){var c=build(h,Direction.NORTH);c.water.fill(new FluidStack(Saltern.SEA_WATER.get(),2000),FluidAction.EXECUTE);step(c,400,true);h.assertTrue(c.cycles()==1&&c.brine.getFluidAmount()==1000&&c.water.getFluidAmount()==1000&&!c.activeBatch(),"1000 mB of water yields exactly 1000 mB brine");h.assertTrue(c.energy()==0,"Exactly 6400 EU consumed for 400 productive ticks");step(c,400,true);h.assertTrue(c.cycles()==2&&c.brine.getFluidAmount()==2000&&c.water.isEmpty(),"Second batch consumes second bucket once");step(c,50,false);h.assertTrue(c.cycles()==2,"No free production without water or power");h.succeed();}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void pauseAndPersistSubmergedBatch(GameTestHelper h){var c=build(h,Direction.EAST);c.water.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE);step(c,150,true);h.assertTrue(c.activeBatch()&&c.visibleWater()==1000&&c.water.isEmpty(),"Escrowed water remains visibly inside the geode basin");int progress=c.progress();step(c,100,false);h.assertTrue(c.progress()==progress,"Power loss freezes growth");var pos=c.getBlockPos();var state=c.getBlockState();var tag=c.saveWithFullMetadata(h.getLevel().registryAccess());c.setRemoved();h.getLevel().removeBlockEntity(pos);var restored=(SalternController)BlockEntity.loadStatic(pos,state,tag,h.getLevel().registryAccess());h.getLevel().setBlockEntity(restored);h.assertTrue(restored.activeBatch()&&restored.progress()==progress,"Water escrow and growth survive reload");step(restored,250,true);h.assertTrue(restored.cycles()==1&&restored.brine.getFluidAmount()==1000,"Reload finishes the same paid batch once");var packet=new SalternController(pos,state);packet.loadWithComponents(restored.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());h.assertTrue(packet.cycles()==1&&packet.brine.getFluidAmount()==1000,"Fluid amounts and harvesting animation synchronize");h.succeed();}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void blockedOutputAndBrokenShellSpendNoPower(GameTestHelper h){var c=build(h,Direction.SOUTH);c.water.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE);step(c,200,true);int progress=c.progress();c.brine.fill(new FluidStack(Saltern.SALT_WATER.get(),7500),FluidAction.EXECUTE);step(c,100,false);h.assertTrue(c.progress()==progress&&c.brine.getFluidAmount()==7500,"Insufficient room pauses the whole batch");c.brine.drain(7500,FluidAction.EXECUTE);var glass=c.at(0,1,0);var old=h.getLevel().getBlockState(glass);h.getLevel().setBlockAndUpdate(glass,Blocks.AIR.defaultBlockState());step(c,40,true);h.assertTrue(c.progress()==progress&&c.energy()==0,"Broken machine cannot accept or spend power");h.getLevel().setBlockAndUpdate(glass,old);step(c,200,true);h.assertTrue(c.brine.getFluidAmount()==1000,"Restored structure finishes without water duplication");h.succeed();}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=100)
 public static void fluidAndEnergySimulationAreSideEffectFree(GameTestHelper h){var c=build(h,Direction.WEST);var input=port(c,-1,1).fluids(null);h.assertTrue(input.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.SIMULATE)==1000&&c.water.isEmpty(),"Simulated water transfer has no effects");input.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE);var energy=port(c,0,2);var fe=energy.fe(energy.facing());h.assertTrue(fe.receiveEnergy(160,true)==160&&c.energy()==0,"Simulated FE transfer has no effects");h.assertTrue(fe.receiveEnergy(160,false)==160&&c.energy()==16,"FE reaches the EU buffer exactly once");h.assertTrue(energy.eu(energy.facing()).receive(1000,false)==48,"FE and EU share the 64 EU/t input limit");h.assertTrue(energy.eu(energy.facing()).receive(1,false)==0,"Repeated calls cannot exceed the input budget");h.succeed();}
 @GameTest(batch="salt-geodes",template="saltern_test",timeoutTicks=150)
 public static void realMiCentrifugeMakesSaltFromChamberBrine(GameTestHelper h){
  var c=build(h,Direction.NORTH);var output=port(c,1,1);var target=output.getBlockPos().relative(output.facing());var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:centrifuge"));h.assertTrue(block!=Blocks.AIR,"Native MI Centrifuge is registered");h.getLevel().setBlockAndUpdate(target,block.defaultBlockState());var machine=(ElectricCraftingMachineBlockEntity)h.getLevel().getBlockEntity(target);
  c.water.fill(new FluidStack(Saltern.SEA_WATER.get(),1000),FluidAction.EXECUTE);step(c,400,true);
  h.assertTrue(c.brine.isEmpty(),"Output hatch exports into actual native MI fluid inventory");
  for(int tick=0;tick<500;tick++){machine.getEnergyComponent().insertEu(32,Simulation.ACT);machine.tick();}
  var salt=BuiltInRegistries.ITEM.get(ResourceLocation.parse("modern_industrialization:salt_dust"));long count=machine.getInventory().getItemStacks().stream().filter(s->s.toStack().is(salt)).mapToLong(s->s.getAmount()).sum();
  h.assertTrue(count==2,"Actual MI processing produces exactly two Salt Dust, got "+count);
  var recipe=h.getLevel().getRecipeManager().byKey(Saltern.id("salt_water_centrifuging")).orElseThrow();h.assertTrue(recipe.value().getType()==aztech.modern_industrialization.machines.init.MIMachineRecipeTypes.CENTRIFUGE,"Recipe uses native MI type");h.succeed();
 }
}
