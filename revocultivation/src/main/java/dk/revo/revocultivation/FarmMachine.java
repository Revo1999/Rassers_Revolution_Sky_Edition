package dk.revo.revocultivation;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.*;

/** Persistent passive first-tier production. Contained mobs never become roaming world entities. */
public class FarmMachine extends BlockEntity implements dk.revo.revolib.energy.IntegratedPowerHost {
    public record FacingIssue(MachineKind.Part part,BlockPos position,Direction expected){}
    public static final int DATA_SIZE=16;
    private int work,cycles,program=-1,state,energy;
    private boolean burnerReady;
    public boolean captureBurner(){if(kind()!=MachineKind.STEEL_HARVESTER||!checkStructure()||program!=24||level.getBlockState(at(0,1,-1)).getValue(FarmFilter.SPECIES)!=4||!burnerReady)return false;burnerReady=false;setChanged();return true;}
    private FacingIssue issue;
    private final ContainedHarvesterMob containedMob=new ContainedHarvesterMob();
    private final ManagedOakTree realTree=new ManagedOakTree();
    private boolean treeWaterPaid;
    public final ItemStackHandler inputs=new ItemStackHandler(2){
        @Override public int getSlotLimit(int slot){return 1;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot==0?kind().acceptsStarter(stack):kind().needsWater()&&stack.is(Items.WATER_BUCKET);}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    public final ItemStackHandler outputs=new ItemStackHandler(9){@Override protected void onContentsChanged(int slot){setChanged();}};
    public final FluidTank water=new FluidTank(kind().waterCapacity(),s->s.is(Fluids.WATER)){@Override protected void onContentsChanged(){setChanged();}};
    public FarmMachine(BlockPos p,BlockState s){super(RevoCultivation.MACHINE.get(),p,s);}
    public MachineKind kind(){return ((FarmController)getBlockState().getBlock()).kind;}
    public Direction facing(){return getBlockState().getValue(FarmController.FACING);}
    public boolean formed(){return getBlockState().getValue(FarmPart.FORMED);}
    public boolean hasIntegratedConverter(BlockPos converter){return kind().electric()&&formed()&&at(kind().radius(),0,kind().radius()).equals(converter);}
    public BlockPos at(int x,int y,int z){return at(facing(),x,y,z);}
    private BlockPos at(Direction front,int x,int y,int z){return worldPosition.relative(front.getClockWise(),x).relative(front,-z-kind().radius()).above(y);}
    public int progress(){return work;}
    public int program(){return program;}
    public int state(){return state;}
    public boolean hasRealTree(){return realTree.exists();}
    public int energy(){return energy;}public int energyCapacity(){return kind().electric()?1600:0;}
    public int receiveEu(long amount,boolean simulate){int accepted=(int)Math.min(Math.max(0,amount),Math.max(0,energyCapacity()-energy));if(!simulate&&accepted>0){energy+=accepted;setChanged();}return accepted;}
    public net.minecraft.world.entity.Mob containedMob(){return kind().isHarvester()?containedMob.get(level,at(0,0,0),program):null;}
    public FacingIssue issue(){return issue;}
    public int buffered(){int amount=0;for(int i=0;i<9;i++)amount+=outputs.getStackInSlot(i).getCount();return amount;}
    public int data(int i){
        var offset=issue==null?BlockPos.ZERO:issue.position().subtract(worldPosition);
        return switch(i){case 0->kind().ordinal();case 1->state;case 2->work;case 3->kind().ticks;case 4->water.getFluidAmount();case 5->program;case 6->issue==null?0:issue.part().ordinal()+1;case 7->offset.getX();case 8->offset.getY();case 9->offset.getZ();case 10->issue==null?0:issue.expected().get3DDataValue();case 11->cycles;case 12->buffered();case 13->energy;case 14->energyCapacity();case 15->kind().energyPerTick();default->0;};
    }
    private boolean blocksMatch(Direction front){
        for(int x=-kind().radius();x<=kind().radius();x++)for(int y=0;y<kind().height;y++)for(int z=-kind().radius();z<=kind().radius();z++){
            var p=at(front,x,y,z);if(!level.hasChunkAt(p))return false;
            var expected=kind().part(x,y,z);var actual=level.getBlockState(p);
            if(expected==MachineKind.Part.AIR?!actual.isAir():!actual.is(kind().block(expected)))return false;
        }
        return true;
    }
    public boolean checkStructure(){
        if(level==null||level.isClientSide)return formed();issue=null;
        if(!blocksMatch(facing())){
            boolean unloaded=false;for(int x=-kind().radius();x<=kind().radius();x++)for(int z=-kind().radius();z<=kind().radius();z++)if(!level.hasChunkAt(at(x,0,z)))unloaded=true;
            for(var front:Direction.Plane.HORIZONTAL)if(front!=facing()&&blocksMatch(front)){issue=new FacingIssue(MachineKind.Part.CONTROLLER,worldPosition,front);break;}
            if(issue==null&&unloaded){state=8;return false;}
            dismantle();state=issue==null?0:7;return false;
        }
        var hatch=at(kind().radius(),0,kind().hatchZ());
        if(level.getBlockState(hatch).getValue(FarmHatch.FACING)!=facing().getClockWise())issue=new FacingIssue(MachineKind.Part.HATCH,hatch,facing().getClockWise());
        if(kind().isHarvester()){var filter=at(0,1,-1);if(level.getBlockState(filter).getValue(FarmFilter.FACING)!=facing())issue=new FacingIssue(MachineKind.Part.FILTER,filter,facing());}
        if(kind().electric()){var converter=at(kind().radius(),0,kind().radius());if(level.getBlockState(converter).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)!=facing().getOpposite())issue=new FacingIssue(MachineKind.Part.POWER,converter,facing().getOpposite());}
        if(issue!=null){dismantle();state=7;return false;}
        formation(facing(),true);return true;
    }
    private void formation(Direction front,boolean formed){
        for(int x=-kind().radius();x<=kind().radius();x++)for(int y=0;y<kind().height;y++)for(int z=-kind().radius();z<=kind().radius();z++){
            var p=at(front,x,y,z);if(!level.hasChunkAt(p))continue;var s=level.getBlockState(p);
            if(kind().part(x,y,z)==MachineKind.Part.POWER&&s.hasProperty(dk.revo.revolib.energy.ConverterAssemblyState.INTEGRATED)){
                if(s.getValue(dk.revo.revolib.energy.ConverterAssemblyState.INTEGRATED)!=formed)level.setBlock(p,s.setValue(dk.revo.revolib.energy.ConverterAssemblyState.INTEGRATED,formed),3);
            }
            if(s.hasProperty(FarmPart.FORMED)&&s.getValue(FarmPart.FORMED)!=formed)level.setBlock(p,s.setValue(FarmPart.FORMED,formed),3);
        }
    }
    public void dismantle(){if(level!=null&&!level.isClientSide){setLights(false);if(kind().realTree()){realTree.clear(level);treeWaterPaid=false;}formation(facing(),false);if(issue!=null&&issue.part()==MachineKind.Part.CONTROLLER)formation(issue.expected(),false);work=0;setChanged();}}
    public boolean needsGrowLights(){
        if(!kind().electric()||level==null)return false;
        long time=Math.floorMod(level.getDayTime(),24000L);
        return (time>=13000&&time<23000)||level.isRaining();
    }
    public boolean growLightsOn(){
        if(!kind().electric()||level==null||!formed())return false;
        var light=level.getBlockState(at(0,2,0));
        return light.is(FarmMaterials.LIGHT.get())&&light.getValue(FarmGrowLight.LIT);
    }
    private void setLights(boolean on){if(!kind().electric()||level==null||level.isClientSide)return;var p=at(0,2,0);if(!level.hasChunkAt(p))return;var s=level.getBlockState(p);if(s.is(FarmMaterials.LIGHT.get())&&s.getValue(FarmGrowLight.LIT)!=on)level.setBlock(p,s.setValue(FarmGrowLight.LIT,on),3);}
    public void dropContents(){for(var inv:List.of(inputs,outputs))for(int i=0;i<inv.getSlots();i++)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),inv.extractItem(i,64,false));}
    public static FarmMachine find(Level level,BlockPos part){
        if(level.getBlockEntity(part) instanceof FarmMachine m)return m;
        for(int x=-4;x<=4;x++)for(int y=-2;y<=0;y++)for(int z=-4;z<=4;z++){
            var p=part.offset(x,y,z);if(!level.hasChunkAt(p)||!(level.getBlockEntity(p) instanceof FarmMachine m))continue;
            Direction front=m.issue!=null&&m.issue.part()==MachineKind.Part.CONTROLLER?m.issue.expected():m.facing();
            for(int a=-m.kind().radius();a<=m.kind().radius();a++)for(int b=0;b<m.kind().height;b++)for(int c=-m.kind().radius();c<=m.kind().radius();c++)
                if(m.kind().part(a,b,c)!=MachineKind.Part.AIR&&m.at(front,a,b,c).equals(part))return m;
        }
        return null;
    }
    private boolean canFit(List<ItemStack> products){
        var copy=new ItemStackHandler(9);for(int i=0;i<9;i++)copy.setStackInSlot(i,outputs.getStackInSlot(i).copy());
        for(var product:products)if(!ItemHandlerHelper.insertItemStacked(copy,product.copy(),false).isEmpty())return false;return true;
    }
    private void export(){if(level.getBlockEntity(at(kind().radius(),0,kind().hatchZ())) instanceof FarmHatchEntity hatch)hatch.eject();}
    private void step(){
        if(!checkStructure())return;
        if(kind().needsWater()&&inputs.getStackInSlot(1).is(Items.WATER_BUCKET)&&water.getSpace()>=1000){
            water.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);inputs.setStackInSlot(1,new ItemStack(Items.BUCKET));
        }
        int selected=kind().program(inputs.getStackInSlot(0),kind().isHarvester()?level.getBlockState(at(0,1,-1)).getValue(FarmFilter.SPECIES):0);
        if(selected!=program){if(kind().realTree()){realTree.clear(level);treeWaterPaid=false;}program=selected;work=0;burnerReady=false;}
        if(kind().isHarvester())containedMob();
        if(program<0){state=2;return;}
        if(kind().needsWater()&&!(kind().realTree()&&treeWaterPaid)&&water.getFluidAmount()<kind().waterPerCycle()){state=3;return;}
        if(kind().isTree()&&!kind().realTree())for(int x=-1;x<=1;x++)for(int y=1;y<=3;y++)for(int z=-1;z<=1;z++)if(!level.getBlockState(at(x,y,z)).isAir()){state=6;return;}
        if(kind().realTree()){
            if(realTree.exists()&&!realTree.intact(level)){realTree.clear(level);work=0;treeWaterPaid=false;state=6;return;}
            if(!realTree.areaClear(level,at(0,0,0))){state=6;return;}
        }
        if(kind().needsDaylight()&&(!level.isDay()||!level.canSeeSky(at(0,kind().realTree()?8:kind().height,0)))){state=4;return;}
        if(program==17&&!level.getBlockState(at(0,-1,0)).is(net.minecraft.world.level.block.Blocks.SOUL_SAND)||program==18&&!level.getBlockState(at(0,-1,0)).is(net.minecraft.world.level.block.Blocks.END_STONE)){state=2;return;}
        var products=MachineKind.products(program,cycles);
        if(kind().electric()||work==0||work+1>=kind().ticks||state==5){if(!canFit(products)){export();if(!canFit(products)){state=5;return;}}}
        if(kind().electric()){
            if(!(level.getBlockEntity(at(kind().radius(),0,kind().radius())) instanceof dk.revo.revolib.energy.DirectConverter converter)||!converter.conversion().feToEu()){state=10;return;}
            if(energy<kind().energyPerTick()){state=9;return;}
            energy-=kind().energyPerTick();
        }
        if(kind().realTree()){
            if(!treeWaterPaid){water.drain(kind().waterPerCycle(),FluidAction.EXECUTE);treeWaterPaid=true;}
            if(work>=kind().ticks/2&&!realTree.exists()&&!realTree.grow(level,at(0,0,0))){state=6;return;}
        }
        state=1;work++;
        if(work>=kind().ticks){
            if(kind().realTree()){
                if(!realTree.exists()||!realTree.intact(level)){state=6;return;}
                realTree.clear(level);treeWaterPaid=false;
            }else if(kind().needsWater())water.drain(kind().waterPerCycle(),FluidAction.EXECUTE);
            for(var product:products)ItemHandlerHelper.insertItemStacked(outputs,product.copy(),false);
            work=0;cycles++;if(program==24)burnerReady=true;export();
            level.levelEvent(2001,at(0,1,0),net.minecraft.world.level.block.Block.getId(kind().isTree()?net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState():kind().isGreenhouse()?net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState():net.minecraft.world.level.block.Blocks.IRON_BARS.defaultBlockState()));
        }
    }
    public static void tick(Level level,BlockPos pos,BlockState block,FarmMachine machine){
        if(level.isClientSide){if(machine.state==1)machine.work=Math.min(machine.kind().ticks-1,machine.work+1);if(machine.kind().isHarvester())machine.containedMob.animate(level.getGameTime(),machine.state==1);return;}
        int oldState=machine.state,oldProgram=machine.program,oldCycles=machine.cycles;machine.step();
        boolean illuminate=machine.formed()&&machine.needsGrowLights()&&(machine.state==1||machine.energy>0);
        // Night/rain lighting: productive ticks include lighting in the existing 8 EU/t; idle LEDs use 1 EU/t.
        if(illuminate&&machine.state!=1)machine.energy--;
        machine.setLights(illuminate);
        if(oldState!=machine.state||oldProgram!=machine.program||oldCycles!=machine.cycles||level.getGameTime()%20==0)level.sendBlockUpdated(pos,machine.getBlockState(),machine.getBlockState(),3);
        machine.setChanged();
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);tag.put("Inputs",inputs.serializeNBT(registries));tag.put("Outputs",outputs.serializeNBT(registries));tag.put("Water",water.writeToNBT(registries,new CompoundTag()));tag.putBoolean("BurnerReady",burnerReady);tag.putInt("Work",work);tag.putInt("Cycles",cycles);tag.putInt("Program",program);tag.putInt("State",state);tag.putInt("EnergyEu",energy);tag.putBoolean("TreeWaterPaid",treeWaterPaid);if(kind().realTree())realTree.save(tag);
        if(kind().isHarvester())tag.put("ContainedMob",containedMob.save());
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);inputs.deserializeNBT(registries,tag.getCompound("Inputs"));outputs.deserializeNBT(registries,tag.getCompound("Outputs"));water.readFromNBT(registries,tag.getCompound("Water"));burnerReady=tag.getBoolean("BurnerReady");work=Math.clamp(tag.getInt("Work"),0,kind().ticks-1);cycles=tag.getInt("Cycles");program=tag.getInt("Program");state=tag.getInt("State");energy=Math.clamp(tag.getInt("EnergyEu"),0,energyCapacity());treeWaterPaid=tag.getBoolean("TreeWaterPaid");if(kind().realTree())realTree.load(tag,at(0,0,0));
        containedMob.load(tag.getCompound("ContainedMob"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
