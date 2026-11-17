package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.*;
@GameTestHolder(RevoCultivation.MOD_ID)
@PrefixGameTestTemplate(false)
public class FarmGameTests {
    static FarmMachine build(GameTestHelper h,MachineKind kind,Direction facing){
        var l=h.getLevel();l.setDayTime(1000);l.updateSkyBrightness();var pos=h.absolutePos(new BlockPos(4,2,3));
        // Each repeated scenario needs a new persistent machine, rather than the previous crop's tank.
        l.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,facing));var m=(FarmMachine)l.getBlockEntity(pos);
        for(int x=-1;x<=1;x++)for(int y=0;y<kind.height;y++)for(int z=-1;z<=1;z++){
            var part=kind.part(x,y,z);var s=kind.block(part).defaultBlockState();if(part==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,facing);if(part==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,facing.getClockWise());if(part==MachineKind.Part.FILTER)s=s.setValue(FarmFilter.FACING,facing);l.setBlockAndUpdate(m.at(x,y,z),s);
        }
        if(kind==MachineKind.TREE)for(int x=-1;x<=1;x++)for(int y=1;y<=3;y++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(m.at(x,y,z),Blocks.AIR.defaultBlockState());
        h.assertTrue(m.checkStructure(),"All required first-tier blocks must form "+kind);return m;
    }
    static void ticks(FarmMachine m,int n){for(int i=0;i<n;i++)FarmMachine.tick(m.getLevel(),m.getBlockPos(),m.getBlockState(),m);}
    static int count(IItemHandler inv,Item item){int n=0;for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(item))n+=inv.getStackInSlot(i).getCount();return n;}
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void allThreeKindsAllRotationsAndFacingErrors(GameTestHelper h){
        for(var kind:new MachineKind[]{MachineKind.TREE,MachineKind.GREENHOUSE,MachineKind.HARVESTER})for(var facing:Direction.Plane.HORIZONTAL){
            var m=build(h,kind,facing);var hatch=m.at(1,0,0);var old=h.getLevel().getBlockState(hatch);
            h.getLevel().setBlockAndUpdate(hatch,old.setValue(FarmHatch.FACING,facing.getCounterClockWise()));
            h.assertTrue(!m.checkStructure()&&m.state()==7&&m.issue().part()==MachineKind.Part.HATCH,"Reversed hatch must report its facing error");
            h.getLevel().setBlockAndUpdate(hatch,old);h.assertTrue(m.checkStructure(),"Fixing hatch must reform machine");
            h.getLevel().setBlockAndUpdate(m.getBlockPos(),m.getBlockState().setValue(FarmController.FACING,facing.getOpposite()));
            h.assertTrue(!m.checkStructure()&&m.state()==7&&m.issue().part()==MachineKind.Part.CONTROLLER&&m.issue().expected()==facing,"Backwards controller must identify correct front");
            h.getLevel().setBlockAndUpdate(m.getBlockPos(),m.getBlockState().setValue(FarmController.FACING,facing));h.assertTrue(m.checkStructure(),"Corrected controller must form again");
            var corner=m.at(-1,0,1);h.getLevel().setBlockAndUpdate(corner,Blocks.AIR.defaultBlockState());h.assertTrue(!m.checkStructure()&&m.state()==0,"Missing support remains incomplete");
        }h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void guidesMatchRealPlacementBlocks(GameTestHelper h){
        for(var kind:new MachineKind[]{MachineKind.TREE,MachineKind.GREENHOUSE,MachineKind.HARVESTER}){
            var m=build(h,kind,Direction.NORTH);var guide=FarmGuideDefinition.create(kind);h.assertTrue(guide.blocks()==(kind==MachineKind.TREE?9:26),"Guide must count all required blocks including controller");
            for(var p:guide.parts()){var actual=h.getLevel().getBlockState(m.getBlockPos().offset(p.offset()));if(actual.hasProperty(FarmPart.FORMED))actual=actual.setValue(FarmPart.FORMED,false);h.assertTrue(actual.equals(p.state()),"Guide mismatch at "+p.offset()+" for "+kind);}
        }h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void treeStarterRetainedAndDaylightClearancePause(GameTestHelper h){
        var m=build(h,MachineKind.TREE,Direction.NORTH);m.inputs.setStackInSlot(0,new ItemStack(Items.OAK_SAPLING));ticks(m,799);h.assertTrue(count(m.outputs,Items.OAK_LOG)==0,"Tree must wait a full growth cycle");ticks(m,1);
        h.assertTrue(count(m.outputs,Items.OAK_LOG)==6&&count(m.outputs,Items.OAK_LEAVES)==2&&count(m.outputs,Items.STICK)==2&&count(m.outputs,Items.OAK_SAPLING)==1,"Tree yields wood and biomass");
        h.assertTrue(m.inputs.getStackInSlot(0).is(Items.OAK_SAPLING)&&m.inputs.getStackInSlot(0).getCount()==1,"Starter sapling must not be consumed");
        h.getLevel().setDayTime(18000);h.getLevel().updateSkyBrightness();ticks(m,50);h.assertTrue(m.state()==4&&m.progress()==0,"Night must pause growth");h.getLevel().setDayTime(1000);h.getLevel().updateSkyBrightness();
        var obstruction=m.at(0,1,0);h.getLevel().setBlockAndUpdate(obstruction,Blocks.STONE.defaultBlockState());ticks(m,1);h.assertTrue(m.state()==6,"Blocked tree volume must be explained");h.getLevel().setBlockAndUpdate(obstruction,Blocks.AIR.defaultBlockState());ticks(m,1);h.assertTrue(m.progress()==1,"Clearing tree space must resume growth");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void greenhouseWaterBucketsFluidsAndAllCrops(GameTestHelper h){
        for(var starter:new Item[]{Items.WHEAT_SEEDS,Items.CARROT,Items.POTATO,Items.BEETROOT_SEEDS}){
            var m=build(h,MachineKind.GREENHOUSE,Direction.NORTH);m.inputs.setStackInSlot(0,new ItemStack(starter));ticks(m,1);h.assertTrue(m.state()==3,"Greenhouse must need water");
            m.inputs.setStackInSlot(1,new ItemStack(Items.WATER_BUCKET));ticks(m,400);h.assertTrue(m.water.getFluidAmount()==750&&m.inputs.getStackInSlot(1).is(Items.BUCKET),"Bucket supplies 1000 mB, cycle consumes only 250 mB");
            h.assertTrue(m.inputs.getStackInSlot(0).is(starter)&&m.data(11)==1,"Crop starter retained, exactly one harvest");
            var fluid=h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,m.at(1,0,0),Direction.EAST);
            h.assertTrue(fluid.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE)==0,"Water input must reject lava");h.assertTrue(fluid.drain(1000,FluidAction.EXECUTE).isEmpty(),"Water port is fill-only");
            h.assertTrue(fluid.fill(new FluidStack(Fluids.WATER,10000),FluidAction.EXECUTE)==7250&&m.water.getFluidAmount()==8000,"Water capacity must be bounded");
            for(var product:MachineKind.products(m.program()))h.assertTrue(count(m.outputs,product.getItem())==product.getCount(),"Correct selected crop output");
        }h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void vanillaHarvesterSpawnerProgramsAndNoRoamingEntities(GameTestHelper h){
        var m=build(h,MachineKind.HARVESTER,Direction.NORTH);h.getLevel().setDayTime(18000);h.getLevel().updateSkyBrightness();
        for(int species=0;species<3;species++){
            m.programSpawner.setStackInSlot(0,SpawnerProgramGameTests.spawner(new String[]{"zombie","skeleton","spider"}[species]));ticks(m,300);
            h.assertTrue(m.program()==20+species&&m.data(11)==species+1,"Spawner must select the actual passive recipe");
            h.assertTrue(m.containedMob().getType()==ContainedHarvesterMob.type(20+species),"Contained mob must be the selected vanilla species");
            for(var product:MachineKind.products(20+species))h.assertTrue(count(m.outputs,product.getItem())>=product.getCount(),"Selected mob-drop recipe produced");
        }
        h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(m.getBlockPos()).inflate(4)).isEmpty(),"Contained mobs must not enter the roaming entity collection");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void containedVanillaMobsPersistWithoutWorldLifecycle(GameTestHelper h){
        var m=build(h,MachineKind.HARVESTER,Direction.NORTH);var l=h.getLevel();
        for(int species=0;species<3;species++){
            m.programSpawner.setStackInSlot(0,SpawnerProgramGameTests.spawner(new String[]{"zombie","skeleton","spider"}[species]));ticks(m,1);
            var mob=m.containedMob();var id=mob.getUUID();
            h.assertTrue(mob==m.containedMob()&&mob.getType()==ContainedHarvesterMob.type(20+species),"Mob instance must be cached and species-specific");
            h.assertTrue(mob.isPersistenceRequired()&&mob.isNoAi()&&mob.isNoGravity()&&mob.isInvulnerable()&&mob.isSilent(),"Contained mob must be persistent and inert");
            if(species==1)h.assertTrue(mob.getMainHandItem().is(Items.BOW),"Vanilla skeleton keeps its bow");
            h.assertTrue(l.getEntity(id)==null,"Contained mob must not be registered as a world entity");
            var saved=m.saveWithFullMetadata(l.registryAccess());
            var restored=(FarmMachine)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),saved,l.registryAccess());restored.setLevel(l);
            h.assertTrue(restored.containedMob().getUUID().equals(id)&&restored.containedMob().getType()==mob.getType(),"Species and actual mob UUID must survive machine reload");
            restored.loadWithComponents(saved,l.registryAccess());h.assertTrue(restored.containedMob().getUUID().equals(id),"Routine data reload preserves contained mob identity");
            var oldDifficulty=l.getDifficulty();l.getServer().setDifficulty(net.minecraft.world.Difficulty.PEACEFUL,true);ticks(m,1200);l.getServer().setDifficulty(oldDifficulty,true);
            h.assertTrue(m.containedMob()==mob&&mob.isAlive()&&!mob.isRemoved()&&mob.tickCount==0,"Peaceful, daylight and elapsed machine cycles cannot despawn, burn or simulate the mob");
            h.assertTrue(mob.position().equals(net.minecraft.world.phys.Vec3.atLowerCornerOf(m.at(0,0,0)).add(.5,5d/16,.5)),"Contained mob stays anchored on the collection tray");
            h.assertTrue(!mob.hurt(l.damageSources().generic(),10),"Contained mob rejects damage");
        }
        // Upgrade old machines with no stored entity, and reject non-selected IDs.
        var old=m.saveWithFullMetadata(l.registryAccess());old.remove("ContainedMob");var migrated=(FarmMachine)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),old,l.registryAccess());migrated.setLevel(l);
        h.assertTrue(migrated.containedMob().getType()==net.minecraft.world.entity.EntityType.SPIDER,"Existing machines migrate to their selected vanilla mob");
        var invalid=new net.minecraft.nbt.CompoundTag();invalid.putString("id","minecraft:wither");old.put("ContainedMob",invalid);migrated.loadWithComponents(old,l.registryAccess());
        h.assertTrue(migrated.containedMob().getType()==net.minecraft.world.entity.EntityType.SPIDER,"Stored IDs cannot override the selected allow-listed species");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void multiProductOutputIsAtomicAndAutoEjects(GameTestHelper h){
        var m=build(h,MachineKind.TREE,Direction.NORTH);m.inputs.setStackInSlot(0,new ItemStack(Items.OAK_SAPLING));for(int i=0;i<8;i++)m.outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));m.outputs.setStackInSlot(8,new ItemStack(Items.OAK_LOG,63));ticks(m,10);
        h.assertTrue(m.state()==5&&m.progress()==0&&count(m.outputs,Items.OAK_LOG)==63,"All products must fit together; no partial production");
        var target=m.at(1,0,0).relative(m.facing().getClockWise());h.getLevel().setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());ticks(m,800);
        var chest=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,target,Direction.WEST);
        h.assertTrue(count(chest,Items.OAK_LOG)==69&&count(chest,Items.COBBLESTONE)==512&&count(chest,Items.OAK_LEAVES)==2,"Blocked recovery and completed cycle must eject without losing or duplicating items");h.assertTrue(m.buffered()==0,"Exported buffer must be empty");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void storageProgressAndWaterSurviveSaving(GameTestHelper h){
        var m=build(h,MachineKind.GREENHOUSE,Direction.NORTH);m.inputs.setStackInSlot(0,new ItemStack(Items.WHEAT_SEEDS));m.water.fill(new FluidStack(Fluids.WATER,2000),FluidAction.EXECUTE);ticks(m,550);
        var restored=(FarmMachine)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),m.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(restored.progress()==150&&restored.data(11)==1&&restored.program()==10,"Cycle progress/program/counter must persist");h.assertTrue(restored.water.getFluidAmount()==1750&&count(restored.outputs,Items.WHEAT)==3&&restored.inputs.getStackInSlot(0).is(Items.WHEAT_SEEDS),"Water, output and retained starter must persist");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void menusAndCapabilitiesKeepRealStorageSafe(GameTestHelper h){
        for(var kind:new MachineKind[]{MachineKind.TREE,MachineKind.GREENHOUSE,MachineKind.HARVESTER}){
            var m=build(h,kind,Direction.NORTH);var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());player.setPos(m.getBlockPos().getX(),m.getBlockPos().getY(),m.getBlockPos().getZ());for(int i=0;i<36;i++)player.getInventory().setItem(i,ItemStack.EMPTY);
            var controller=new FarmControllerMenu(1,player.getInventory(),m);var hatch=(FarmHatchEntity)h.getLevel().getBlockEntity(m.at(1,0,0));var menu=new FarmHatchMenu(2,player.getInventory(),hatch,m);
            h.assertTrue(controller.slots.size()==(kind.isHarvester()?37:0)&&menu.slots.size()==kind.inputSlots()+45,"Harvester controller exposes its retained program; other storage stays in hatch");int outputs=kind.inputSlots();int playerStart=outputs+9;
            h.assertTrue(menu.getSlot(playerStart).x==8&&menu.getSlot(playerStart).y==kind.hatchHeight()-82,"Standard player inventory coordinates");
            m.outputs.setStackInSlot(0,new ItemStack(Items.OAK_LOG,8));h.assertTrue(!menu.getSlot(outputs).mayPlace(new ItemStack(Items.DIRT)),"Output slots reject insertion");h.assertTrue(menu.quickMoveStack(player,outputs).getCount()==8&&player.getInventory().countItem(Items.OAK_LOG)==8,"Shift-click extracts actual output");
            var cached=hatch.items();m.outputs.setStackInSlot(0,new ItemStack(Items.STICK,5));h.getLevel().setBlockAndUpdate(m.at(-1,0,1),Blocks.AIR.defaultBlockState());m.checkStructure();
            h.assertTrue(!menu.stillValid(player)&&cached.extractItem(2,5,false).isEmpty()&&count(m.outputs,Items.STICK)==5,"Dismantled hatches cannot access storage through stale menu/capability");h.assertTrue(controller.stillValid(player),"Controller stays readable for repair diagnostics");
        }h.succeed();
    }
}
