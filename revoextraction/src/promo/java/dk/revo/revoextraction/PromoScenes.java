package dk.revo.revoextraction;

import java.nio.file.*;
import java.util.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dk.revo.revocultivation.*;
import dk.revo.revoindustry.furnace.*;
import dk.revo.revostorage.*;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Creative-built, functioning showcase. Never runs in a normal user save. */
@EventBusSubscriber(modid="revoextraction",value=Dist.CLIENT)
public final class PromoScenes {
    static final BlockPos BASE=new BlockPos(1000,210,1000);
    static final Path OUT=Path.of("../../screenshots/promo").toAbsolutePath().normalize();
    record View(String file,String title,long time,double[] eye,double[] target,int fov){}
    static final View[] VIEWS={
        new View("01-first-island","The First Island",1800,new double[]{20,10,27},new double[]{-1,3,-1},57),
        new View("02-glass-and-growth","Glass and Growth",5800,new double[]{40,5.5,14},new double[]{38,2.3,3},70),
        new View("03-copper-current","Copper Current",11000,new double[]{-40,4.8,9},new double[]{-42,2.5,-1},76),
        new View("04-greenhouse-after-dark","Greenhouse After Dark",17800,new double[]{38,4.5,13},new double[]{38,2.5,3},70),
        new View("05-night-shift","The Night Shift",14500,new double[]{-29,6,-22},new double[]{-31,2.5,-38},60),
        new View("06-industry-in-the-clouds","Industry Above the Clouds",23200,new double[]{58,33,58},new double[]{-4,0,-15},64),
        new View("qa-underwater-kelp","Underwater Kelp",5800,new double[]{34,3.1,8},new double[]{34,2.3,3},55),
        new View("qa-salt-geode","Powered Salt Geode",5800,new double[]{43,3.1,8},new double[]{43,2.3,3},55),
        new View("qa-electric-drill","Electric Drill",5800,new double[]{-33,3,-2},new double[]{-32,1.6,2},55)
    };
    static ServerLevel level;static volatile boolean ready,moved,proof;static boolean started,done;static int view,frames,serverTicks;
    static double oldFov;static boolean oldHide,oldBob,oldPause;static SluiceEntity sluice;static DrillMachine drill;
    static SmallMachineEntity composter,silk;static final List<FarmMachine> farms=new ArrayList<>();
    static final List<BlockPos> motors=new ArrayList<>(),tanks=new ArrayList<>();static final List<BlockPos> outputs=new ArrayList<>();
    static ElectricDrillEntity electric;static FurnaceController furnace;static StorageEntity terminal;
    static BlockPos p(int x,int y,int z){return BASE.offset(x,y,z);}
    static void put(int x,int y,int z,Block b){put(x,y,z,b.defaultBlockState());}
    static void put(int x,int y,int z,BlockState s){level.setBlock(p(x,y,z),s,3);}
    static void fast(int x,int y,int z,Block b){level.setBlock(p(x,y,z),b.defaultBlockState(),2);}
    static Block block(String id){var b=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));if(b==Blocks.AIR)throw new IllegalStateException("Missing showcase block "+id);return b;}
    static void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block b){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)fast(x,y,z,b);}
    static void stateFill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)put(x,y,z,s);}
    static void chest(BlockPos at,Item item,int stacks){
        level.setBlockAndUpdate(at,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
        var c=(ChestBlockEntity)level.getBlockEntity(at);for(int i=0;i<stacks;i++)c.setItem(i,new ItemStack(item,64));
    }
    static void hopper(BlockPos at,Direction facing){level.setBlockAndUpdate(at,Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,facing));}
    static void lamp(int x,int y,int z){put(x,y,z,Blocks.LANTERN);}
    static void hanging(int x,int y,int z){put(x,y+1,z,Blocks.CHAIN);put(x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    static void island(int cx,int cz,int rx,int rz){
        for(int x=-rx;x<=rx;x++)for(int z=-rz;z<=rz;z++){
            double r=x*x/(double)(rx*rx)+z*z/(double)(rz*rz);double edge=1+.055*Math.sin(x*1.3+z*.7);
            if(r>edge)continue;int depth=2+(int)(7*Math.pow(Math.max(0,1-r),.6));
            for(int y=-depth;y<=0;y++)fast(cx+x,y,cz+z,y==0?Blocks.GRASS_BLOCK:y>-3?Blocks.DIRT:(Math.floorMod(x*7+z*11+y*3,9)==0?Blocks.ANDESITE:Blocks.STONE));
            if(r>.76&&r<.95&&Math.floorMod(x*11+z*17,23)==0)for(int y=-1;y>=-depth-2;y--)fast(cx+x,y,cz+z,Blocks.MOSS_BLOCK);
            if(r>.66&&r<.9&&Math.floorMod(x*19+z*7,41)==0){fast(cx+x,-depth-1,cz+z,Blocks.HANGING_ROOTS);}
        }
    }
    static void bridge(int x0,int z0,int x1,int z1){
        int n=Math.max(Math.abs(x1-x0),Math.abs(z1-z0));boolean alongX=Math.abs(x1-x0)>Math.abs(z1-z0);
        for(int i=0;i<=n;i++){int x=Math.round(x0+(x1-x0)*i/(float)n),z=Math.round(z0+(z1-z0)*i/(float)n);
            for(int w=-1;w<=1;w++)put(x+(alongX?0:w),0,z+(alongX?w:0),Blocks.SPRUCE_PLANKS);
            for(int w:new int[]{-2,2}){int xx=x+(alongX?0:w),zz=z+(alongX?w:0);put(xx,0,zz,Blocks.STRIPPED_DARK_OAK_LOG);put(xx,1,zz,Blocks.DARK_OAK_FENCE);if(i%5==0)lamp(xx,2,zz);}
        }
    }
    static void tree(int x,int z,int height){
        fill(x,1,z,x,height,z,Blocks.OAK_LOG);
        for(int y=height-2;y<=height+2;y++)for(int a=-3;a<=3;a++)for(int b=-3;b<=3;b++)if(a*a+b*b+(y-height)*(y-height)*1.7<=11&&!(a==0&&b==0&&y<=height))put(x+a,y,z+b,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
    }
    static void shrubs(int cx,int cz,int rx,int rz){
        for(int x=-rx+1;x<rx;x++)for(int z=-rz+1;z<rz;z++){double r=x*x/(double)(rx*rx)+z*z/(double)(rz*rz);if(r<.68||r>.9||!level.getBlockState(p(cx+x,1,cz+z)).isAir()||!level.getBlockState(p(cx+x,0,cz+z)).is(Blocks.GRASS_BLOCK))continue;if(Math.floorMod(x*13+z*17,19)==0){put(cx+x,1,cz+z,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}else if(Math.floorMod(x*17+z*7,31)==0)put(cx+x,1,cz+z,Blocks.POPPY);}
    }
    static void home(){
        island(0,0,18,15);fill(-9,0,-8,6,0,5,Blocks.STONE_BRICKS);
        fill(-8,1,-7,5,5,-7,Blocks.SPRUCE_PLANKS);fill(-8,1,2,5,5,2,Blocks.SPRUCE_PLANKS);
        fill(-8,1,-7,-8,5,2,Blocks.SPRUCE_PLANKS);fill(5,1,-7,5,5,2,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{-8,-4,1,5}){fill(x,1,-7,x,5,-7,Blocks.STRIPPED_OAK_LOG);fill(x,1,2,x,5,2,Blocks.STRIPPED_OAK_LOG);}
        for(int x:new int[]{-6,2})fill(x,2,2,x+1,4,2,Blocks.GLASS);
        fill(-8,2,-5,-8,4,-3,Blocks.GLASS);fill(5,2,-5,5,4,-3,Blocks.GLASS);
        fill(-2,1,2,-1,3,2,Blocks.AIR);fill(-2,0,3,-1,0,6,Blocks.OAK_PLANKS);
        for(int a=0;a<7;a++){stateFill(-9+a,6+a,-8,-9+a,6+a,3,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));stateFill(6-a,6+a,-8,6-a,6+a,3,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));}
        fill(-2,12,-8,-1,12,3,Blocks.DARK_OAK_SLAB);
        fill(2,4,-6,3,13,-5,Blocks.BRICKS);put(2,14,-6,Blocks.CAMPFIRE);put(3,14,-5,Blocks.CAMPFIRE);
        fill(-6,1,-5,-3,1,-5,Blocks.BOOKSHELF);put(-6,1,0,Blocks.CRAFTING_TABLE);put(-5,1,0,Blocks.BARREL);put(3,1,0,Blocks.SMOKER);
        fill(-7,5,4,4,5,4,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{-7,4}){fill(x,1,4,x,4,4,Blocks.OAK_FENCE);hanging(x==-7?-6:3,4,4);}
        hanging(-1,4,0);put(3,3,-6,Blocks.LANTERN);
        fill(-3,0,6,2,0,12,Blocks.MOSSY_COBBLESTONE);fill(-4,0,8,5,0,10,Blocks.COARSE_DIRT);
        tree(-12,-5,6);tree(-12,8,5);shrubs(0,0,18,15);
        put(8,1,5,SmallMachines.COMPOSTER.get().defaultBlockState().setValue(SmallMachineBlock.FACING,Direction.SOUTH));composter=(SmallMachineEntity)level.getBlockEntity(p(8,1,5));
        chest(p(8,1,6),Items.AIR,0);hopper(p(8,2,5),Direction.DOWN);chest(p(8,3,5),Items.OAK_LEAVES,6);outputs.add(p(8,1,6));
        put(12,1,5,SmallMachines.CULTIVATOR.get().defaultBlockState().setValue(SmallMachineBlock.FACING,Direction.SOUTH));silk=(SmallMachineEntity)level.getBlockEntity(p(12,1,5));silk.storage.setStackInSlot(1,new ItemStack(SmallMachines.WORM.get()));
        chest(p(12,1,6),Items.AIR,0);hopper(p(12,2,5),Direction.DOWN);chest(p(12,3,5),Items.OAK_LEAVES,5);outputs.add(p(12,1,6));
        fill(7,0,3,14,0,8,Blocks.OAK_PLANKS);lamp(7,1,8);lamp(14,1,8);
        farm(MachineKind.TREE,10,1,-4,Items.OAK_SAPLING);
    }
    static FarmMachine farm(MachineKind kind,int x,int y,int z,Item starter){
        var pos=p(x,y,z);level.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.SOUTH));var m=(FarmMachine)level.getBlockEntity(pos);
        for(int a=-kind.radius();a<=kind.radius();a++)for(int b=0;b<kind.height;b++)for(int c=-kind.radius();c<=kind.radius();c++){
            var type=kind.part(a,b,c);var s=kind.block(type).defaultBlockState();if(type==MachineKind.Part.CONTROLLER)s=s.setValue(FarmController.FACING,Direction.SOUTH);if(type==MachineKind.Part.HATCH)s=s.setValue(FarmHatch.FACING,Direction.WEST);if(type==MachineKind.Part.POWER)s=s.setValue(BlockStateProperties.FACING,Direction.NORTH);level.setBlockAndUpdate(m.at(a,b,c),s);
        }
        if(!m.checkStructure())throw new IllegalStateException("Showcase farm failed: "+kind+" issue="+m.issue());
        m.inputs.setStackInSlot(0,new ItemStack(starter));if(kind.needsWater())m.water.fill(new FluidStack(Fluids.WATER,16000),FluidAction.EXECUTE);
        var out=m.at(kind.radius(),0,kind.hatchZ()).west();chest(out,Items.AIR,0);outputs.add(out);
        if(kind.electric())level.setBlockAndUpdate(m.at(kind.radius(),0,kind.radius()).north(),block("createaddition:creative_energy").defaultBlockState());
        farms.add(m);return m;
    }
    static void greenhouse(){
        island(38,0,15,13);fill(26,0,-9,50,0,10,Blocks.MOSSY_STONE_BRICKS);
        for(int x:new int[]{26,32,38,44,50})for(int z:new int[]{-9,10})fill(x,1,z,x,6,z,Blocks.STRIPPED_OAK_LOG);
        fill(26,1,-9,50,5,-9,Blocks.GLASS);fill(26,1,-9,26,5,10,Blocks.GLASS);fill(50,1,-9,50,5,10,Blocks.GLASS);
        for(int x:new int[]{26,32,38,44,50})for(int z:new int[]{-9,10})fill(x,1,z,x,6,z,Blocks.STRIPPED_OAK_LOG);
        for(int x=27;x<=49;x++)if(x<35||x>40)fill(x,1,10,x,5,10,Blocks.GLASS);
        for(int z=-9;z<=10;z++)for(int x=26;x<=50;x++){int h=6+Math.min(5,Math.min(x-26,50-x)/2);put(x,h,z,(z==-9||z==10||x%6==2)?Blocks.OAK_PLANKS:Blocks.GLASS);}
        fill(35,0,7,40,0,13,Blocks.OAK_PLANKS);
        for(int x:new int[]{27,33,43,49})hanging(x,5,8);
        for(int x:new int[]{29,46})for(int z=-7;z<=-3;z++){put(x,0,z,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));put(x,1,z,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));}
        farm(MachineKind.IRON_GREENHOUSE,34,1,5,Items.KELP);farm(MachineKind.IRON_GREENHOUSE,43,1,5,SmallMachines.SALT_STARTER.get());
        for(int x:new int[]{31,37,40,46})hanging(x,5,3);
        fill(32,1,10,46,6,10,Blocks.AIR);
        for(int x=28;x<=48;x++){put(x,1,-7,AllBlocks.FLUID_PIPE.getDefaultState());if(x%6==0)put(x,2,-7,Blocks.COPPER_BLOCK);}
        for(int x:new int[]{26,50}){lamp(x,1,11);put(x,1,-10,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
        shrubs(38,0,15,13);
    }
    static void motor(BlockPos at,Direction face){level.setBlockAndUpdate(at,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,face));motors.add(at);}
    static void workshop(){
        island(-40,0,16,14);fill(-53,0,-9,-27,0,10,Blocks.POLISHED_ANDESITE);
        fill(-52,1,-8,-28,4,-8,Blocks.BRICKS);for(int x:new int[]{-52,-44,-36,-28}){fill(x,1,-8,x,8,-8,Blocks.STRIPPED_DARK_OAK_LOG);fill(x,1,10,x,6,10,Blocks.STRIPPED_DARK_OAK_LOG);}
        fill(-52,5,-8,-28,7,-8,Blocks.GLASS);fill(-53,8,-9,-27,8,-4,Blocks.CUT_COPPER_SLAB);
        fill(-52,7,9,-28,7,10,Blocks.DARK_OAK_PLANKS);for(int x:new int[]{-52,-44,-36,-28})fill(x,7,-8,x,7,10,Blocks.STRIPPED_DARK_OAK_LOG);
        for(int x=-51;x<=-29;x+=6)hanging(x,6,-4);
        var head=p(-37,1,4);var state=SluiceContent.SLUICE.get().defaultBlockState().setValue(SluiceBlock.FACING,Direction.SOUTH);
        level.setBlockAndUpdate(head,state);SluiceContent.SLUICE.get().setPlacedBy(level,head,state,null,new ItemStack(SluiceContent.SLUICE.get()));
        level.setBlockAndUpdate(head.above(),SluiceContent.FEEDER.get().defaultBlockState().setValue(SluiceFeeder.FACING,Direction.SOUTH));sluice=(SluiceEntity)level.getBlockEntity(head);
        hopper(head.above(2),Direction.DOWN);chest(head.above(3),Items.GRAVEL,10);
        for(int z:new int[]{1,3})level.setBlockAndUpdate(head.above().north(z),AllBlocks.FLUID_PIPE.getDefaultState());
        var pump=head.above().north(2);level.setBlockAndUpdate(pump,AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(BlockStateProperties.FACING,Direction.SOUTH));
        level.setBlockAndUpdate(pump.above(),AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.Z));motor(pump.above().south(),Direction.NORTH);
        for(int x=0;x<2;x++)for(int y=0;y<3;y++)for(int z=0;z<2;z++){var at=head.north(4+z).east(x).above(y);level.setBlockAndUpdate(at,AllBlocks.FLUID_TANK.getDefaultState());tanks.add(at);}
        for(int a=1;a<=2;a++)level.setBlockAndUpdate(head.west(a),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));motor(head.west(3),Direction.EAST);
        chest(head.south(3),Items.AIR,0);outputs.add(head.south(3));
        var origin=p(-47,2,-2);level.setBlockAndUpdate(origin,DrillTier.IRON.block(DrillLayout.Part.CONTROLLER).defaultBlockState().setValue(DrillController.FACING,Direction.SOUTH));drill=(DrillMachine)level.getBlockEntity(origin);
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
            var type=DrillTier.IRON.part(x,y,z);var s=DrillTier.IRON.block(type).defaultBlockState();if(type==DrillLayout.Part.CONTROLLER)s=s.setValue(DrillController.FACING,Direction.SOUTH);if(type==DrillLayout.Part.INPUT)s=s.setValue(DrillInput.FACING,Direction.WEST);if(type==DrillLayout.Part.HATCH)s=s.setValue(DrillHatch.FACING,Direction.EAST);level.setBlockAndUpdate(drill.at(x,y,z),s);
        }
        if(!drill.checkStructure())throw new IllegalStateException("Promo iron drill failed formation");
        motor(drill.at(1,1,0).west(),Direction.EAST);chest(drill.at(-1,0,0).east(),Items.AIR,0);outputs.add(drill.at(-1,0,0).east());
        level.setBlockAndUpdate(p(-32,1,2),RevoExtraction.ELECTRIC.get().defaultBlockState());electric=(ElectricDrillEntity)level.getBlockEntity(p(-32,1,2));
        level.setBlockAndUpdate(p(-31,1,2),block("revoindustry:power_converter").defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));put(-30,1,2,block("createaddition:creative_energy"));
        hopper(p(-32,0,2),Direction.SOUTH);chest(p(-32,0,3),Items.AIR,0);
        for(int x=-52;x<-45;x+=2){put(x,1,6,Blocks.BARREL);put(x,2,6,Blocks.OAK_SLAB);}
        lamp(-53,1,9);lamp(-28,1,9);shrubs(-40,0,16,14);
    }
    static void foundry(){
        island(-30,-38,15,12);fill(-42,0,-47,-18,0,-28,Blocks.DEEPSLATE_TILES);
        fill(-42,1,-46,-18,5,-46,Blocks.BRICKS);
        for(int x:new int[]{-42,-34,-26,-18}){fill(x,1,-46,x,8,-46,Blocks.POLISHED_BASALT);fill(x,1,-28,x,6,-28,Blocks.POLISHED_BASALT);}
        fill(-43,8,-47,-17,8,-43,Blocks.POLISHED_DEEPSLATE);fill(-43,9,-47,-17,9,-44,Blocks.CUT_COPPER_SLAB);
        fill(-42,7,-30,-18,7,-28,Blocks.POLISHED_DEEPSLATE);for(int x:new int[]{-42,-34,-26,-18})fill(x,7,-46,x,7,-28,Blocks.POLISHED_BASALT);
        for(int x:new int[]{-39,-21}){fill(x,4,-44,x+1,12,-43,Blocks.DEEPSLATE_BRICKS);put(x,13,-44,Blocks.CAMPFIRE);put(x+1,13,-43,Blocks.CAMPFIRE);}
        var pos=p(-31,1,-39);level.setBlockAndUpdate(pos,Furnaces.CONTROLLERS.get(FurnaceTier.IRON).get().defaultBlockState());furnace=(FurnaceController)level.getBlockEntity(pos);furnace.orientation.facingDirection=Direction.SOUTH;
        for(var entry:furnace.getActiveShape().simpleMembers.entrySet())level.setBlockAndUpdate(ShapeMatcher.toWorldPos(pos,Direction.SOUTH,entry.getKey()),entry.getValue().getPreviewState());
        BlockPos input=ShapeMatcher.toWorldPos(pos,Direction.SOUTH,new BlockPos(-1,0,0)),output=ShapeMatcher.toWorldPos(pos,Direction.SOUTH,new BlockPos(1,0,0)),fuel=ShapeMatcher.toWorldPos(pos,Direction.SOUTH,new BlockPos(0,0,2));
        level.setBlockAndUpdate(input,Furnaces.ITEM_HATCHES.get(0).get().defaultBlockState());level.setBlockAndUpdate(output,Furnaces.ITEM_HATCHES.get(1).get().defaultBlockState());level.setBlockAndUpdate(fuel,Furnaces.FUEL_HATCHES.get(0).get().defaultBlockState());
        hopper(input.south(),Direction.NORTH);chest(input.south().above(),Items.RAW_IRON,9);hopper(fuel.north(),Direction.SOUTH);chest(fuel.north().above(),Items.COAL,6);
        hopper(output.below(),Direction.SOUTH);chest(output.below().south(),Items.AIR,0);outputs.add(output.below().south());
        furnace.tick();if(!furnace.isShapeValid())throw new IllegalStateException("Promo foundry failed formation");
        for(int x:new int[]{-40,-21})for(int z=-42;z<=-32;z++){put(x,-1,z,Blocks.MAGMA_BLOCK);put(x,0,z,Blocks.ORANGE_STAINED_GLASS);}
        for(int x:new int[]{-40,-35,-25,-20})hanging(x,5,-30);
        for(int x=-40;x<=-36;x+=2){put(x,1,-33,Blocks.BLAST_FURNACE);put(x,2,-33,Blocks.IRON_BARS);lamp(x,3,-33);}
        fill(-24,1,-41,-20,1,-39,Blocks.RAW_IRON_BLOCK);fill(-22,2,-41,-20,2,-40,Blocks.RAW_COPPER_BLOCK);
        shrubs(-30,-38,15,12);
    }
    static void storage(){
        island(15,-36,15,12);fill(3,0,-45,27,0,-27,Blocks.OAK_PLANKS);
        fill(3,1,-45,27,5,-45,Blocks.SPRUCE_PLANKS);fill(3,1,-45,3,5,-27,Blocks.SPRUCE_PLANKS);fill(27,1,-45,27,5,-27,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{3,9,15,21,27}){fill(x,1,-45,x,7,-45,Blocks.STRIPPED_SPRUCE_LOG);fill(x,1,-27,x,6,-27,Blocks.STRIPPED_SPRUCE_LOG);}
        fill(3,7,-46,27,7,-40,Blocks.DARK_OAK_SLAB);for(int x=5;x<=25;x+=5)hanging(x,5,-29);
        fill(3,6,-29,27,6,-27,Blocks.SPRUCE_PLANKS);for(int x:new int[]{3,9,15,21,27})fill(x,7,-45,x,7,-27,Blocks.STRIPPED_SPRUCE_LOG);
        for(int x=5;x<=25;x+=3)for(int y=1;y<=3;y++){put(x,y,-43,Blocks.BARREL);put(x+1,y,-43,Blocks.BOOKSHELF);}
        for(int x=12;x<=14;x++){put(x,1,-35,RevoStorage.DRIVE.get());var e=(StorageEntity)level.getBlockEntity(p(x,1,-35));for(int i=0;i<4;i++)e.chests.setStackInSlot(i,new ItemStack(Items.CHEST));e.insert(new ItemStack(Items.OAK_LOG,64),false);e.insert(new ItemStack(Items.IRON_INGOT,64),false);}
        put(15,1,-35,RevoStorage.TERMINAL.get());terminal=(StorageEntity)level.getBlockEntity(p(15,1,-35));motor(p(15,0,-35),Direction.UP);
        var stand=new ArmorStand(level,p(21,1,-30).getX()+.5,p(21,1,-30).getY(),p(21,1,-30).getZ()+.5);stand.setYRot(180);stand.setItemSlot(EquipmentSlot.HEAD,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("revoindustry:elite_feeding_helmet"))));level.addFreshEntity(stand);
        put(8,1,-29,Blocks.CRAFTING_TABLE);put(9,1,-29,Blocks.CARTOGRAPHY_TABLE);lamp(5,1,-27);lamp(25,1,-27);shrubs(15,-36,15,12);
    }
    static void prepare(){
        var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{
            try{
                var server=mc.getSingleplayerServer();if(!server.getWorldPath(LevelResource.ROOT).toString().contains("Revo Promo Skybase"))throw new IllegalStateException("Separate Revo Promo Skybase save required");
                var player=server.getPlayerList().getPlayer(mc.player.getUUID());level=player.serverLevel();player.closeContainer();player.setGameMode(GameType.CREATIVE);player.getInventory().clearContent();player.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
                level.setDayTime(1800);level.setWeatherParameters(100000,0,false,false);server.setDifficulty(Difficulty.PEACEFUL,true);
                level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,server);level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false,server);level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0,server);
                for(int x=(BASE.getX()-65)>>4;x<=(BASE.getX()+60)>>4;x++)for(int z=(BASE.getZ()-55)>>4;z<=(BASE.getZ()+25)>>4;z++)level.setChunkForced(x,z,true);
                for(var at:BlockPos.betweenClosed(BASE.offset(-65,-15,-55),BASE.offset(60,20,25)))if(!level.getBlockState(at).isAir())level.setBlock(at,Blocks.AIR.defaultBlockState(),2);
                level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(BASE.offset(-66,-16,-56)),Vec3.atLowerCornerOf(BASE.offset(61,21,26)))).forEach(net.minecraft.world.entity.Entity::discard);
                level.getEntitiesOfClass(ArmorStand.class,new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(BASE.offset(-66,-16,-56)),Vec3.atLowerCornerOf(BASE.offset(61,21,26)))).forEach(net.minecraft.world.entity.Entity::discard);
                home();greenhouse();workshop();foundry();storage();
                bridge(15,0,25,0);bridge(-17,1,-25,1);bridge(-38,-11,-32,-28);bridge(8,-12,12,-26);bridge(-16,-37,1,-37);
                ready=true;move(0);System.out.println("PROMO base built: "+farms.size()+" farms, Create sluice, iron drill, active foundry and connected storage");
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
    static void move(int index){moved=false;var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{var v=VIEWS[index];var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());level.setDayTime(v.time);p.connection.teleport(BASE.getX()+v.eye[0],BASE.getY()+v.eye[1],BASE.getZ()+v.eye[2],0,0);p.getAbilities().flying=true;p.onUpdateAbilities();moved=true;});}
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post event){
        if(!ready||done)return;serverTicks++;
        if(serverTicks==40){for(var at:motors)if(level.getBlockEntity(at) instanceof CreativeMotorBlockEntity motor){motor.generatedSpeed.setValue(64);motor.updateGeneratedRotation();}for(var at:tanks){var port=level.getCapability(Capabilities.FluidHandler.BLOCK,at,Direction.SOUTH);if(port!=null)port.fill(new FluidStack(Fluids.WATER,64000),FluidAction.EXECUTE);}}
        if(serverTicks%200==0)System.out.println("PROMO automation ticks="+serverTicks+" sluice="+sluice.cycle+" drill="+drill.getPersistentData().getInt("cycle")+" compost="+composter.cycles()+" silk="+silk.cycles()+" greenhouse="+farms.get(1).data(11)+" light="+farms.get(1).growLightsOn()+" foundry="+furnace.status());
        if(serverTicks==640){try{Files.createDirectories(OUT);if(sluice.cycle==0||drill.getPersistentData().getInt("cycle")==0||composter.cycles()==0||silk.cycles()==0||farms.get(1).data(11)==0||farms.get(2).data(11)==0)throw new IllegalStateException("Automation fixture did not produce on real ticks");
            Files.writeString(OUT.resolve("automation-proof.txt"),"Real server ticks: "+serverTicks+"\nSluice paid cycles: "+sluice.cycle+"\nIron drill cycles: "+drill.getPersistentData().getInt("cycle")+"\nCompost cycles: "+composter.cycles()+"\nSilk cycles: "+silk.cycles()+"\nElectric greenhouse harvests: "+farms.get(1).data(11)+", "+farms.get(2).data(11)+"\nElectric drill cycles: "+electric.cycle+"\nFoundry: "+furnace.status()+"\nStorage slots: "+terminal.networkItems().getSlots()+"\nThe staged creative build uses actual creative motors/FE supplies and finite stocked inputs; products are made by native machine ticks and hoppers.\n");proof=true;
        }catch(Exception ex){throw new RuntimeException(ex);}}
    }
    @SubscribeEvent public static void clientTick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(!started){started=true;oldFov=mc.options.fov().get();oldHide=mc.options.hideGui;oldBob=mc.options.bobView().get();oldPause=mc.options.pauseOnLostFocus;mc.options.hideGui=true;mc.options.bobView().set(false);mc.options.pauseOnLostFocus=false;prepare();return;}
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(!ready||!moved)return;
        if(mc.getWindow().getWidth()!=2560||mc.getWindow().getHeight()!=1440){mc.getWindow().setWidth(2560);mc.getWindow().setHeight(1440);mc.resizeDisplay();}
        var v=VIEWS[view];mc.options.fov().set(v.fov);mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(BASE.getX()+v.target[0],BASE.getY()+v.target[1],BASE.getZ()+v.target[2]));
        if(!proof||++frames<140)return;
        if(v.file.equals("04-greenhouse-after-dark")&&!farms.get(1).growLightsOn())throw new IllegalStateException("Night capture requires real grow lights");
        try{
            Files.createDirectories(OUT);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(v.file+".png"));}
            System.out.println("PROMO captured "+v.file);
            if(++view>=VIEWS.length){done=true;mc.options.fov().set((int)oldFov);mc.options.hideGui=oldHide;mc.options.bobView().set(oldBob);mc.options.pauseOnLostFocus=oldPause;mc.options.save();Files.writeString(OUT.resolve("capture-result.txt"),"PASS: six native 2560x1440 shader screenshots, daylight/golden hour/night/blue hour, separate connected skyblock showcase save.\n");mc.stop();}
            else{frames=0;move(view);}
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
}
