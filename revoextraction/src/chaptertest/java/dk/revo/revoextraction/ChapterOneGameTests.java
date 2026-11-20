package dk.revo.revoextraction;
import dk.revo.revoindustry.*;
import dk.revo.revocultivation.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
@GameTestHolder(RevoExtraction.MOD_ID)
@PrefixGameTestTemplate(false)
public class ChapterOneGameTests{
 static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){var p=FakePlayerFactory.getMinecraft(h.getLevel());p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();return p;}
 static PrimitiveVesselEntity vessel(GameTestHelper h,boolean rain){var p=h.absolutePos(new BlockPos(4,rain?24:3,4));h.getLevel().setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(p,(rain?PrimitiveMachines.BARREL.get():PrimitiveMachines.CRUCIBLE.get()).defaultBlockState());return (PrimitiveVesselEntity)h.getLevel().getBlockEntity(p);}
 static int drops(GameTestHelper h,Item item,BlockPos p){return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(2)).stream().filter(e->e.getItem().is(item)).mapToInt(e->e.getItem().getCount()).sum();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void miningHammerReplacesNormalDrop(GameTestHelper h){var p=player(h);var pos=h.absolutePos(new BlockPos(4,2,4));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+2);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(HammerItems.WOOD.get()));h.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());h.assertTrue(p.gameMode.destroyBlock(pos),"Mine dirt with actual server game mode");h.assertTrue(drops(h,HammerItems.PEBBLE.get(),pos)==4&&drops(h,Items.DIRT,pos)==0,"One dirt gives four pebbles, never dirt as well");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void hammerProcessingChainAndRightClick(GameTestHelper h){var p=player(h);var pos=h.absolutePos(new BlockPos(4,2,4));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+2);var tool=new ItemStack(HammerItems.STONE.get());p.setItemInHand(InteractionHand.MAIN_HAND,tool);var blocks=new Block[]{Blocks.COBBLESTONE,Blocks.GRAVEL,Blocks.SAND};var items=new Item[]{Items.GRAVEL,Items.SAND,HammerItems.DUST.get().asItem()};for(int i=0;i<3;i++){h.getLevel().setBlockAndUpdate(pos,blocks[i].defaultBlockState());var result=tool.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));h.assertTrue(result.consumesAction()&&h.getLevel().getBlockState(pos).isAir()&&drops(h,items[i],pos)==1,"Right-click conversion "+i);}h.assertTrue(tool.getDamageValue()==3,"Every successful conversion costs durability");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void crookCollectsLeavesAndGuaranteesFirstReplacement(GameTestHelper h){var p=player(h);var pos=h.absolutePos(new BlockPos(4,2,4));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+2);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(RevoCultivation.CROOK.get()));h.getLevel().setBlockAndUpdate(pos,Blocks.OAK_LEAVES.defaultBlockState());h.assertTrue(p.gameMode.destroyBlock(pos),"Crook mines through real drop pipeline");h.assertTrue(drops(h,Items.OAK_LEAVES,pos)==1&&drops(h,Items.OAK_SAPLING,pos)>=1,"Leaf block and replacement sapling must be available without shears");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100)
 public static void rainRewardFillsEnoughWaterForFirstClay(GameTestHelper h) {
  var barrel=vessel(h,true);
  var level=h.getLevel();
  var plains=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
  level.getChunkAt(barrel.getBlockPos()).fillBiomesFromNoise((x,y,z,sampler)->plains,level.getChunkSource().randomState().sampler());
  var server=level.getServer();
  server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),"weather rain 300s");
  h.assertTrue(server.getWorldData().overworldData().getRainTime()==6000,"The actual quest command sets 6000 ticks of rain");
  h.runAfterDelay(50,()->{
   level.setRainLevel(1);
   for(int batch=0;batch<3;batch++){
    for(int tick=0;tick<200;tick++)barrel.tick();
    h.assertTrue(barrel.tank.getFluidAmount()==1000&&barrel.getBlockState().getValue(PrimitiveVesselBlock.LEVEL)==8,"Open sky fills a bucket in 200 ticks");
    barrel.inventory.setStackInSlot(0,new ItemStack(HammerItems.DUST.get()));
    h.assertTrue(barrel.mix(),"Each full rain barrel makes one clay block");
   }
   var clay=barrel.inventory.getStackInSlot(1);
   h.assertTrue(clay.is(Items.CLAY)&&clay.getCount()==3,"Three barrels provide enough clay for the first bucket and crucible");
   level.setBlockAndUpdate(barrel.getBlockPos().above(),Blocks.STONE.defaultBlockState());
   for(int tick=0;tick<200;tick++)barrel.tick();
   h.assertTrue(barrel.tank.isEmpty(),"A roof blocks collection");
   h.succeed();
  });
 }
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void dustAndWaterMixAtomicallyIntoClay(GameTestHelper h){var m=vessel(h,true);m.inventory.setStackInSlot(0,new ItemStack(HammerItems.DUST.get()));m.tank.fill(new FluidStack(Fluids.WATER,999),FluidAction.EXECUTE);h.assertTrue(!m.mix()&&m.inventory.getStackInSlot(0).getCount()==1,"Partial water cannot consume dust");m.tank.fill(new FluidStack(Fluids.WATER,1),FluidAction.EXECUTE);h.assertTrue(m.mix()&&m.tank.isEmpty()&&m.inventory.getStackInSlot(0).isEmpty()&&m.inventory.getStackInSlot(1).is(Items.CLAY),"One dust plus 1000 water becomes one clay block");m.inventory.setStackInSlot(1,new ItemStack(Items.CLAY,64));m.inventory.setStackInSlot(0,new ItemStack(HammerItems.DUST.get()));m.tank.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);h.assertTrue(!m.mix()&&m.tank.getFluidAmount()==1000&&m.inventory.getStackInSlot(0).getCount()==1,"Full output cannot void water or dust");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void crucibleRequiresHeatAndMeltsFourCobble(GameTestHelper h){var m=vessel(h,false);m.inventory.setStackInSlot(0,new ItemStack(Items.COBBLESTONE,4));for(int i=0;i<250;i++)m.tick();h.assertTrue(m.tank.isEmpty()&&m.progress()==0,"Cold crucible must pause");h.getLevel().setBlockAndUpdate(m.getBlockPos().below(2),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(m.getBlockPos().below(),Blocks.TORCH.defaultBlockState());for(int i=0;i<799;i++)m.tick();h.assertTrue(m.tank.getFluidAmount()==750&&m.inventory.getStackInSlot(0).getCount()==1,"Each cobble requires a full melt step");m.tick();h.assertTrue(m.tank.getFluidAmount()==1000&&m.tank.getFluid().is(Fluids.LAVA)&&m.inventory.getStackInSlot(0).isEmpty(),"Four cobble become one bucket at 40 seconds on a torch");h.getLevel().setBlockAndUpdate(m.getBlockPos().below(),Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT,false));h.assertTrue(m.heat()==0,"Unlit campfire is cold");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void clayBucketsDoNotCreateIronBuckets(GameTestHelper h){var m=vessel(h,true);var p=player(h);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrimitiveMachines.BUCKET.get()));m.tank.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);h.assertTrue(m.bucket(p,InteractionHand.MAIN_HAND)&&p.getMainHandItem().is(PrimitiveMachines.WATER_BUCKET.get())&&m.tank.isEmpty(),"Water pickup yields clay water bucket");h.assertTrue(m.bucket(p,InteractionHand.MAIN_HAND)&&p.getMainHandItem().is(PrimitiveMachines.BUCKET.get())&&m.tank.getFluidAmount()==1000,"Water pour returns clay bucket");var lava=vessel(h,false);lava.tank.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE);h.assertTrue(lava.bucket(p,InteractionHand.MAIN_HAND)&&p.getMainHandItem().is(PrimitiveMachines.LAVA_BUCKET.get()),"Lava pickup yields clay lava bucket");h.assertTrue(p.getInventory().countItem(Items.BUCKET)==0,"No iron bucket is minted");h.succeed();}
 @GameTest(batch="early-machines",template="drill_test",timeoutTicks=100) public static void vesselSaveReloadAndFluidCaps(GameTestHelper h){var m=vessel(h,false);m.inventory.setStackInSlot(0,new ItemStack(Items.COBBLESTONE,3));m.tank.fill(new FluidStack(Fluids.LAVA,500),FluidAction.EXECUTE);h.assertTrue(m.tank.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0,"Crucible rejects water");var tag=m.saveWithFullMetadata(h.getLevel().registryAccess());var restored=(PrimitiveVesselEntity)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(restored!=null&&restored.tank.getFluidAmount()==500&&restored.inventory.getStackInSlot(0).getCount()==3,"Contents survive reload");var fluid=h.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,m.getBlockPos(),Direction.UP);h.assertTrue(fluid!=null&&fluid.drain(1000,FluidAction.SIMULATE).getAmount()==500&&m.tank.getFluidAmount()==500,"Fluid simulation is non-destructive");h.succeed();}
}

