package dk.revo.revoindustry.hatchery;

import java.util.*;
import dk.revo.revolib.energy.EnergyInterop;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class HatcheryGameTests {
    private static HatcheryController build(GameTestHelper h,Direction facing){
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(6,2,6));
        for(int x=0;x<13;x++)for(int y=1;y<9;y++)for(int z=0;z<13;z++)level.setBlockAndUpdate(h.absolutePos(new BlockPos(x,y,z)),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(origin,Hatchery.CONTROLLER.get().defaultBlockState().setValue(HatcheryBlock.FACING,facing));
        for(var c:HatcheryLayout.SHELL)level.setBlockAndUpdate(HatcheryLayout.at(origin,facing,c.offset()),HatcheryLayout.state(c,facing));
        return (HatcheryController)level.getBlockEntity(origin);
    }
    private static void step(HatcheryController c,int ticks,boolean power){
        var level=c.getLevel();
        for(int i=0;i<ticks;i++){
            level.getServer().getWorldData().overworldData().setGameTime(level.getGameTime()+1);
            if(power)c.receiveEu(Hatchery.EU_PER_TICK,false);
            c.tick();
        }
    }
    private static ChamberWither actor(HatcheryController c){var list=c.getLevel().getEntitiesOfClass(ChamberWither.class,new AABB(c.at(0,2,2)).inflate(3),w->w.belongsTo(c));return list.isEmpty()?null:list.getFirst();}
    private static int stars(HatcheryController c){int n=0;for(int i=0;i<9;i++){var s=c.output().inventory.getStackInSlot(i);if(s.is(Items.NETHER_STAR))n+=s.getCount();}return n;}
    private static void egg(HatcheryController c,int count){c.input().inventory.setStackInSlot(0,new ItemStack(Hatchery.EGG.get(),count));}
    private static void ritual(GameTestHelper h,BlockPos foot,Direction axis,boolean lastBase){
        var level=h.getLevel();
        level.setBlockAndUpdate(foot.below(),Blocks.HOPPER.defaultBlockState());
        if(!lastBase)level.setBlockAndUpdate(foot,Blocks.SOUL_SAND.defaultBlockState());
        for(int x=-1;x<=1;x++)level.setBlockAndUpdate(foot.relative(axis,x).above(),(x==0?Blocks.SOUL_SOIL:Blocks.SOUL_SAND).defaultBlockState());
        for(int x=-1;x<=1;x++)level.setBlockAndUpdate(foot.relative(axis,x).above(2),Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
        if(lastBase)level.setBlockAndUpdate(foot,Blocks.SOUL_SAND.defaultBlockState());
    }
    @GameTest(template="hatchery_test",timeoutTicks=120)
    public static void automatedRitualBothAxesLastBaseAndHopper(GameTestHelper h){
        var level=h.getLevel();var a=h.absolutePos(new BlockPos(3,3,3));var b=h.absolutePos(new BlockPos(9,3,9));
        ritual(h,a,Direction.EAST,false);ritual(h,b,Direction.SOUTH,true);
        h.assertTrue(level.getBlockState(a).isAir()&&level.getBlockState(b).isAir(),"Both ritual bases consumed without a player");
        var eggs=level.getEntitiesOfClass(ItemEntity.class,new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(a),net.minecraft.world.phys.Vec3.atLowerCornerOf(b)).inflate(4),e->e.getItem().is(Hatchery.EGG.get()));
        h.assertTrue(eggs.stream().mapToInt(e->e.getItem().getCount()).sum()==2,"Exactly one egg per seven-block ritual");
        h.assertTrue(level.getEntitiesOfClass(WitherBoss.class,new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(a),net.minecraft.world.phys.Vec3.atLowerCornerOf(b)).inflate(4)).isEmpty(),"Ritual must not spawn a roaming Wither");
        // Calling vanilla skull completion again must not duplicate a converted ritual.
        WitherSkullBlock.checkSpawn(level,a.above(2));
        int total=0;
        for(var foot:List.of(a,b)){
            var hopper=(HopperBlockEntity)level.getBlockEntity(foot.below());
            h.assertTrue(HopperBlockEntity.suckInItems(level,hopper),"Native hopper suction accepts the ritual's dropped item");
            for(int i=0;i<5;i++)if(hopper.getItem(i).is(Hatchery.EGG.get()))total+=hopper.getItem(i).getCount();
        }
        h.assertTrue(total==2,"Ordinary hoppers collect both dropped eggs");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=100)
    public static void incompleteRitualDoesNotConsumeIngredients(GameTestHelper h){
        var foot=h.absolutePos(new BlockPos(4,3,4));h.getLevel().setBlockAndUpdate(foot,Blocks.SOUL_SAND.defaultBlockState());
        for(int x=-1;x<=1;x++)h.getLevel().setBlockAndUpdate(foot.offset(x,1,0),Blocks.SOUL_SAND.defaultBlockState());
        for(int x=-1;x<=0;x++)h.getLevel().setBlockAndUpdate(foot.offset(x,2,0),Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
        h.assertTrue(!WitherEggRitual.convert(h.getLevel(),foot)&&h.getLevel().getBlockState(foot).is(Blocks.SOUL_SAND),"Two skulls cannot consume the base");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=200)
    public static void allRotationsStructureAndCachedCapabilityGuards(GameTestHelper h){
        for(var facing:Direction.Plane.HORIZONTAL){
            var c=build(h,facing);h.assertTrue(c.validate(),"5x5x5 must form facing "+facing+": "+c.problem());
            h.assertTrue(HatcheryLayout.SHELL.size()==98,"Hollow shell contains 98 blocks");
            var input=c.input().items(Direction.UP);h.assertTrue(input.insertItem(0,new ItemStack(Items.STONE),false).is(Items.STONE),"Input rejects non-eggs");
            h.assertTrue(input.insertItem(0,new ItemStack(Hatchery.EGG.get()),true).isEmpty()&&!c.input().hasEgg(),"Simulated insertion has no side effects");
            var glass=c.at(0,2,0);var state=h.getLevel().getBlockState(glass);h.getLevel().setBlockAndUpdate(glass,Blocks.AIR.defaultBlockState());
            h.assertTrue(!c.validate()&&input.insertItem(0,new ItemStack(Hatchery.EGG.get()),false).is(Hatchery.EGG.get()),"Cached handlers cannot access a broken chamber");
            h.getLevel().setBlockAndUpdate(glass,state);h.assertTrue(c.validate(),"Restoring glass reforms");
            var out=c.output();h.getLevel().setBlockAndUpdate(out.getBlockPos(),out.getBlockState().setValue(HatcheryBlock.FACING,out.facing().getOpposite()));h.assertTrue(!c.validate()&&c.problem().startsWith("Wrong facing"),"Wrong-facing hatch reports orientation");
        }h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=150)
    public static void energySimulationRateLimitAndNoPowerEscrow(GameTestHelper h){
        var c=build(h,Direction.NORTH);egg(c,2);step(c,10,false);h.assertTrue(c.stage()==0&&c.input().inventory.getStackInSlot(0).getCount()==2,"No power leaves eggs untouched");
        var port=(HatcheryPort)h.getLevel().getBlockEntity(c.at(0,0,4));var fe=port.fe(Direction.SOUTH);
        h.assertTrue(port.fe(Direction.NORTH)==null&&port.eu(Direction.NORTH)==null,"Only outward energy face connects");
        int ratio=EnergyInterop.fePerEu();h.assertTrue(fe.receiveEnergy(8192*ratio,true)==8192*ratio&&c.energy()==0,"Energy simulation is side-effect free");
        h.assertTrue(fe.receiveEnergy(8192*ratio,false)==8192*ratio&&c.energy()==8192,"FE converts to EU once");
        var eu=port.eu(Direction.SOUTH);h.assertTrue(eu.receive(Integer.MAX_VALUE,false)==8192*3&&eu.receive(1,false)==0,"FE/EU share one per-tick input budget");
        step(c,1,false);h.assertTrue(c.stage()==1&&c.input().inventory.getStackInSlot(0).getCount()==1,"Exactly one egg enters escrow");
        h.assertTrue(c.output().items(null).insertItem(0,new ItemStack(Items.NETHER_STAR),false).is(Items.NETHER_STAR),"Output rejects external insertion");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=300)
    public static void actualWitherHatchesTakesDamageAndProducesOneStar(GameTestHelper h){
        var c=build(h,Direction.NORTH);egg(c,1);step(c,Hatchery.INCUBATION_TICKS,true);
        var wither=actor(c);h.assertTrue(c.stage()==2&&wither!=null&&wither instanceof WitherBoss&&wither.isAlive(),"A real live Wither exists in the world after egg incubation");
        var packetCopy=new HatcheryController(c.getBlockPos(),c.getBlockState());packetCopy.loadWithComponents(c.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(packetCopy.status()==5&&packetCopy.stage()==2,"Ordinary block-entity packets synchronize animation and roof-light operating state");
        h.assertTrue(wither.isNoAi()&&wither.getInvulnerableTicks()==0,"No autonomous attacks or spawn explosion");
        step(c,Hatchery.HATCH_TICKS,true);h.assertTrue(c.stage()==3&&wither.getScale()>.7,"Hatching grows the contained entity");
        step(c,20,true);h.assertTrue(wither.getHealth()<wither.getMaxHealth()&&stars(c)==0,"Chamber must damage the actual Wither before granting any star");
        step(c,Hatchery.KILL_TICKS-20,true);h.assertTrue(wither.isDeadOrDying()&&stars(c)==1&&c.stage()==0,"One lethal real hit yields exactly one saved star");
        h.assertTrue(c.energy()==0,"Exactly 1500 productive ticks at 8192 EU/t consume 12,288,000 EU");
        step(c,50,false);h.assertTrue(stars(c)==1,"No second result without another egg");
        var loose=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(c.at(0,2,2)).inflate(4),e->e.getItem().is(Items.NETHER_STAR));h.assertTrue(loose.isEmpty(),"Suppressed world loot cannot duplicate the hatch output");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=300)
    public static void powerStructureAndOutputPauseTheLivingWither(GameTestHelper h){
        var c=build(h,Direction.EAST);egg(c,1);step(c,Hatchery.INCUBATION_TICKS+Hatchery.HATCH_TICKS+20,true);
        var wither=actor(c);float health=wither.getHealth();int progress=c.progress();
        for(int x=-1;x<=1;x++)h.assertTrue(h.getLevel().getBlockState(c.at(x,4,2)).getValue(HatcheryBlock.ACTIVE),"Roof downlights illuminate the active egg/Wither chamber");
        step(c,40,false);h.assertTrue(c.progress()==progress&&wither.getHealth()==health&&stars(c)==0,"Power loss pauses actual Wither damage");
        for(int x=-1;x<=1;x++)h.assertTrue(!h.getLevel().getBlockState(c.at(x,4,2)).getValue(HatcheryBlock.ACTIVE),"Roof lights stop consuming/claiming power during an outage");
        h.assertTrue(!wither.hurt(h.getLevel().damageSources().genericKill(),1000)&&wither.isAlive(),"Outside damage cannot steal/duplicate the machine reward");
        var glass=c.at(0,2,0);var state=h.getLevel().getBlockState(glass);h.getLevel().setBlockAndUpdate(glass,Blocks.AIR.defaultBlockState());
        step(c,20,true);h.assertTrue(c.progress()==progress&&wither.isAlive(),"A broken facade pauses without releasing or losing the Wither");
        h.getLevel().setBlockAndUpdate(glass,state);
        for(int i=0;i<9;i++)c.output().inventory.setStackInSlot(i,new ItemStack(Items.STONE,64));
        int energy=c.energy();step(c,30,false);h.assertTrue(c.progress()==progress&&c.energy()==energy&&wither.getHealth()==health,"Full output spends no power and deals no damage");
        c.output().inventory.setStackInSlot(0,ItemStack.EMPTY);step(c,Hatchery.KILL_TICKS-progress,true);h.assertTrue(stars(c)==1,"Restoring output completes the same job exactly once");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=300)
    public static void reloadKeepsOwnedActorAndBreakReturnsEggOnce(GameTestHelper h){
        var c=build(h,Direction.WEST);egg(c,1);step(c,Hatchery.INCUBATION_TICKS+Hatchery.HATCH_TICKS+20,true);
        var actor=actor(c);var id=actor.getUUID();var tag=c.saveWithFullMetadata(h.getLevel().registryAccess());var pos=c.getBlockPos();var state=c.getBlockState();
        c.setRemoved();h.getLevel().removeBlockEntity(pos);
        var restored=(HatcheryController)BlockEntity.loadStatic(pos,state,tag,h.getLevel().registryAccess());h.getLevel().setBlockEntity(restored);
        step(restored,1,true);h.assertTrue(actor(restored).getUUID().equals(id),"Reload reuses the same owned Wither UUID");
        restored.breakMachine();restored.breakMachine();h.assertTrue(actor.isRemoved()&&stars(restored)==0,"Dismantling removes the owned actor without producing a star");
        var eggs=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),e->e.getItem().is(Hatchery.EGG.get()));h.assertTrue(eggs.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Escrowed egg can be returned only once");h.succeed();
    }
    @GameTest(template="hatchery_test",timeoutTicks=100)
    public static void recipesAndAutomaticOutputUseRealNativeInventories(GameTestHelper h){
        var c=build(h,Direction.NORTH);var output=c.output();var dest=output.getBlockPos().relative(output.facing());h.getLevel().setBlockAndUpdate(dest,Blocks.CHEST.defaultBlockState());
        output.commitStar();c.export();var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(dest);h.assertTrue(chest.getItem(0).is(Items.NETHER_STAR)&&stars(c)==0,"Output hatch automatically exports into a real chest");
        var cap=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,c.input().getBlockPos(),Direction.UP);h.assertTrue(cap!=null&&cap.insertItem(0,new ItemStack(Hatchery.EGG.get()),false).isEmpty(),"Hopper/pipe item capability inserts an egg");
        for(var cell:HatcheryLayout.SHELL)h.assertTrue(h.getLevel().getRecipeManager().byKey(Hatchery.id(HatcheryLayout.block(cell.part()).builtInRegistryHolder().key().location().getPath())).isPresent(),"Every chamber part has a loaded native recipe");h.succeed();
    }
}
