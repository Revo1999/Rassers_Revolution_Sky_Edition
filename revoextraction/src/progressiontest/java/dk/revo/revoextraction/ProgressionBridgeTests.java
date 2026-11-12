package dk.revo.revoextraction;
import dk.revo.revocultivation.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public final class ProgressionBridgeTests {
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));}
 static FarmMachine farm(GameTestHelper h,MachineKind kind,int species){
  var l=h.getLevel();var pos=h.absolutePos(new BlockPos(4,2,4));l.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.NORTH));var m=(FarmMachine)l.getBlockEntity(pos);
  for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
   var part=kind.part(x,y,z);var s=kind.block(part).defaultBlockState();if(part==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,Direction.NORTH);if(part==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,Direction.EAST);if(part==MachineKind.Part.FILTER)s=s.setValue(FarmFilter.FACING,Direction.NORTH).setValue(FarmFilter.SPECIES,species);l.setBlockAndUpdate(m.at(x,y,z),s);
  }h.assertTrue(m.checkStructure(),"Harvester forms");return m;
 }
 static void ticks(FarmMachine m,int n){for(int i=0;i<n;i++)FarmMachine.tick(m.getLevel(),m.getBlockPos(),m.getBlockState(),m);}
 static int count(net.neoforged.neoforge.items.IItemHandler inv,Item i){int n=0;for(int s=0;s<inv.getSlots();s++)if(inv.getStackInSlot(s).is(i))n+=inv.getStackInSlot(s).getCount();return n;}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeCokeMillingRecipeLoadsBeforeDiamonds(GameTestHelper h){
  var r=h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("revoextraction:milling/coke_dust")).orElseThrow().value();
  h.assertTrue(r.getType()==com.simibubi.create.AllRecipeTypes.MILLING.getType(),"Native Create milling recipe");h.assertTrue(r.getIngredients().getFirst().test(new ItemStack(item("modern_industrialization:coke"))),"Accepts early coke");h.assertTrue(r.getResultItem(h.getLevel().registryAccess()).is(item("modern_industrialization:coke_dust")),"Produces usable MI coke dust");h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void cowProducesLeatherAndFoodWithSavedContainedMob(GameTestHelper h){
  var m=farm(h,MachineKind.HARVESTER,3);ticks(m,300);h.assertTrue(count(m.outputs,Items.LEATHER)==1&&count(m.outputs,Items.BEEF)==2,"Wooden cow produces leather before steel");var mob=m.containedMob();h.assertTrue(mob.getType()==EntityType.COW&&mob.isNoAi()&&h.getLevel().getEntity(mob.getUUID())==null,"Cow stays machine-owned");
  var saved=m.saveWithoutMetadata(h.getLevel().registryAccess());m.loadWithComponents(saved,h.getLevel().registryAccess());h.assertTrue(m.containedMob().getUUID().equals(mob.getUUID())&&count(m.outputs,Items.LEATHER)==1,"Save/load preserves cow and inventory");h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void steelBlazeUnlockAndWoodenTierGuard(GameTestHelper h){
  h.assertTrue(MachineKind.HARVESTER.program(ItemStack.EMPTY,5)==-1,"Wood cannot select blaze even with edited block state");var m=farm(h,MachineKind.STEEL_HARVESTER,4);ticks(m,150);h.assertTrue(m.program()==24&&count(m.outputs,Items.BLAZE_ROD)==1,"Steel blaze produces a rod per cycle");h.assertTrue(m.containedMob().getType()==EntityType.BLAZE&&m.containedMob().isNoAi()&&h.getLevel().getEntity(m.containedMob().getUUID())==null,"Blaze cannot roam, attack or create extra world drops");h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void filterCyclesAndFullStorageAreSafe(GameTestHelper h){
  for(var kind:new MachineKind[]{MachineKind.HARVESTER,MachineKind.STEEL_HARVESTER}){
   int last=kind==MachineKind.HARVESTER?4:10;var m=farm(h,kind,last);var at=m.at(0,1,-1);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);h.getLevel().getBlockState(at).useWithoutItem(h.getLevel(),player,new BlockHitResult(Vec3.atCenterOf(at),Direction.NORTH,at,false));h.assertTrue(h.getLevel().getBlockState(at).getValue(FarmFilter.SPECIES)==0,"Correct tier-specific selector wraps");
   h.getLevel().setBlockAndUpdate(at,h.getLevel().getBlockState(at).setValue(FarmFilter.SPECIES,last));if(kind==MachineKind.STEEL_HARVESTER)m.inputs.setStackInSlot(0,new ItemStack(Items.NETHERITE_INGOT));for(int i=0;i<9;i++)m.outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));ticks(m,kind.ticks*2);h.assertTrue(m.state()==5&&m.progress()==0,"Full inventory prevents all products and progress");
  }h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void rareFluxHasStableCadenceAndNoWoodenBypass(GameTestHelper h){
  var flux=item("fluxnetworks:flux_dust");h.assertTrue(flux!=Items.AIR,"Flux Networks test fixture installed");for(var tier:DrillTier.values()){
   int found=0;for(int i=0;i<384;i++)if(tier.output(i)==flux)found++;h.assertTrue(found==(tier==DrillTier.WOODEN?0:3),"Exactly one rare result per 128 successful ore cycles for "+tier);
   if(tier!=DrillTier.WOODEN){h.assertTrue(tier.output(126)!=flux&&tier.output(127)==flux&&tier.output(128)!=flux,"Rare boundary is stable");}
  }h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void rareFluxSurvivesBlockedOutputAndReload(GameTestHelper h){
  var l=h.getLevel();var tier=DrillTier.IRON;var pos=h.absolutePos(new BlockPos(4,2,4));l.setBlockAndUpdate(pos,tier.block(DrillLayout.Part.CONTROLLER).defaultBlockState().setValue(DrillController.FACING,Direction.NORTH));var m=(DrillMachine)l.getBlockEntity(pos);
  for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
   var part=tier.part(x,y,z);var s=tier.block(part).defaultBlockState();if(part==DrillLayout.Part.CONTROLLER)s=s.setValue(DrillController.FACING,Direction.NORTH);if(part==DrillLayout.Part.INPUT)s=s.setValue(DrillInput.FACING,Direction.EAST);if(part==DrillLayout.Part.HATCH)s=s.setValue(DrillHatch.FACING,Direction.WEST);l.setBlockAndUpdate(m.at(x,y,z),s);
  }h.assertTrue(m.checkStructure(),"Iron drill forms");((DrillDrive)l.getBlockEntity(m.at(1,1,0))).setSpeed(32);m.getPersistentData().putInt("cycle",127);for(int i=0;i<9;i++)m.inventory.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
  for(int i=0;i<40;i++)DrillMachine.tick(l,pos,m.getBlockState(),m);h.assertTrue(m.getPersistentData().getInt("cycle")==127&&m.progress()==0,"Blocked rare item is neither discarded nor rerolled");var saved=m.saveWithoutMetadata(l.registryAccess());m.getPersistentData().putInt("cycle",0);m.loadWithComponents(saved,l.registryAccess());h.assertTrue(m.getPersistentData().getInt("cycle")==127,"Rare cursor is persistent");m.inventory.setStackInSlot(8,ItemStack.EMPTY);
  for(int i=0;i<19;i++)DrillMachine.tick(l,pos,m.getBlockState(),m);h.assertTrue(count(m.inventory,item("fluxnetworks:flux_dust"))==1&&m.getPersistentData().getInt("cycle")==128,"One rare dust emitted exactly once after storage reopens");h.succeed();
 }
}
