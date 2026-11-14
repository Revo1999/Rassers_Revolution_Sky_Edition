package dk.revo.revoextraction;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.*;
public class DrillMachine extends BlockEntity {
    public enum StructureState { INCOMPLETE, VALID, UNLOADED, WRONG_FACING }
    public record FacingIssue(DrillLayout.Part part,BlockPos position,Direction expected) {}
    private StructureState structureState=StructureState.INCOMPLETE;
    private FacingIssue facingIssue;
    private float work;
    private int dryTicks;
    private float coolantRemainder;
    private boolean broken;
    public static final int DRY_LIMIT=100,WATER_PER_CYCLE=100;
    public float clientAngle;
    public float previousClientAngle;
    private float workingSpeed;
    public final ItemStackHandler inventory = new ItemStackHandler(9) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    public DrillMachine(BlockPos p,BlockState s){super(RevoExtraction.MACHINE.get(),p,s);}
    public Direction facing(){return getBlockState().getValue(DrillController.FACING);}
    public BlockPos at(int x,int y,int z){return worldPosition.relative(facing().getClockWise(),x).relative(facing(),z-1).offset(0,y-1,0);}
    private BlockPos at(Direction front,int x,int y,int z){return worldPosition.relative(front.getClockWise(),x).relative(front,z-1).offset(0,y-1,0);}
    public FacingIssue facingIssue(){return facingIssue;}
    public boolean formed(){return getBlockState().getValue(DrillPart.FORMED);}
    public float speed(){return workingSpeed;}
    public DrillTier tier(){return getBlockState().is(RevoExtraction.STEEL_CONTROLLER.get())?DrillTier.STEEL:getBlockState().is(RevoExtraction.IRON_CONTROLLER.get())?DrillTier.IRON:DrillTier.WOODEN;}
    public DrillWaterTankEntity waterTank(){return level!=null&&level.hasChunkAt(at(-1,1,0))&&level.getBlockEntity(at(-1,1,0)) instanceof DrillWaterTankEntity tank?tank:null;}
    public int waterAmount(){var tank=waterTank();return tier()==DrillTier.STEEL&&tank!=null?tank.water.getFluidAmount():0;}
    public int dryTicks(){return dryTicks;}
    public boolean broken(){return broken;}
    public void restoreDamage(boolean seized,int dry){broken=seized;dryTicks=Math.clamp(dry,0,DRY_LIMIT);setChanged();}
    public String repairFailure(){
        if(!broken)return "not_broken";
        if(!checkStructure())return "incomplete";
        if(inputSpeed()!=0)return "stop_drive";
        if(waterAmount()<WATER_PER_CYCLE)return "need_water";
        return null;
    }
    public void repair(){broken=false;dryTicks=0;work=0;coolantRemainder=0;setSpeed(0);setChanged();sendUpdate();}
    public int progress() { return Math.clamp((int)(work*1200/tier().workPerItem), 0, 1200); }
    private ItemStack nextOutput(){return new ItemStack(tier().output(getPersistentData().getInt("cycle")));}
    public float inputSpeed() {
        if(level==null || !level.hasChunkAt(at(1,1,0))) return 0;
        return level.getBlockEntity(at(1,1,0)) instanceof DrillDrive drive ? drive.getSpeed() : 0;
    }
    // Codes are synchronized by the menu: incomplete, working, no drive, output blocked, unloaded, wrong facing.
    public int operationalState() {
        if(structureState==StructureState.UNLOADED)return 4;
        if(!formed()) return structureState==StructureState.WRONG_FACING?5:0;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(!level.hasChunkAt(at(x,0,z)))return 4;
        if(broken)return 8;
        if(inputSpeed()==0) return 2;
        if(Math.abs(inputSpeed())<tier().minimumRpm) return 6;
        var next=nextOutput();
        if(!ItemHandlerHelper.insertItemStacked(inventory,next,true).isEmpty())return 3;
        return tier()==DrillTier.STEEL&&waterAmount()<coolantNeeded(Math.abs(inputSpeed()))?7:1;
    }
    private boolean matchesBlock(int x,int y,int z,BlockState s){
        var part=tier().part(x,y,z);
        return part==DrillLayout.Part.AIR?s.isAir():s.is(tier().block(part));
    }

    public boolean checkStructure(){
        if(level==null || level.isClientSide) return formed();
        facingIssue=null;
        boolean blocksCorrect=true,unloaded=false;
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++) {
            var p=at(x,y,z);
            if(!level.hasChunkAt(p)){unloaded=true;blocksCorrect=false;continue;}
            if(!matchesBlock(x,y,z,level.getBlockState(p)))blocksCorrect=false;
        }
        if(!blocksCorrect){
            // A backwards controller changes the blueprint origin. Try the other fronts before
            // calling an otherwise correct structure incomplete.
            for(Direction front:Direction.Plane.HORIZONTAL){
                if(front==facing())continue;
                boolean match=true;
                for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
                    var p=at(front,x,y,z);
                    if(!level.hasChunkAt(p)||!matchesBlock(x,y,z,level.getBlockState(p)))match=false;
                }
                if(match){facingIssue=new FacingIssue(DrillLayout.Part.CONTROLLER,worldPosition,front);break;}
            }
            if(facingIssue==null&&unloaded){structureState=StructureState.UNLOADED;return false;}
            dismantle();structureState=facingIssue==null?StructureState.INCOMPLETE:StructureState.WRONG_FACING;return false;
        }
        var inputPos=at(1,1,0);var hatchPos=at(-1,0,0);
        if(level.getBlockState(inputPos).getValue(DrillInput.FACING)!=facing().getClockWise())
            facingIssue=new FacingIssue(DrillLayout.Part.INPUT,inputPos,facing().getClockWise());
        else if(level.getBlockState(hatchPos).getValue(DrillHatch.FACING)!=facing().getCounterClockWise())
            facingIssue=new FacingIssue(DrillLayout.Part.HATCH,hatchPos,facing().getCounterClockWise());
        if(facingIssue!=null){dismantle();structureState=StructureState.WRONG_FACING;return false;}
        structureState=StructureState.VALID;setFormation(true);return true;
    }
    private void setFormation(boolean value){
        setFormation(facing(),value);
    }
    private void setFormation(Direction front,boolean value){
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
            var p=at(front,x,y,z); if(!level.hasChunkAt(p))continue; var s=level.getBlockState(p);
            if(s.hasProperty(DrillPart.FORMED) && s.getValue(DrillPart.FORMED)!=value) level.setBlock(p,s.setValue(DrillPart.FORMED,value),3);
        }
    }
    public void dismantle(){if(level!=null&&!level.isClientSide){setFormation(false);if(facingIssue!=null&&facingIssue.part()==DrillLayout.Part.CONTROLLER)setFormation(facingIssue.expected(),false);work=0;setSpeed(0);setChanged();}}
    public void dropContents(){for(int i=0;i<9;i++)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),inventory.extractItem(i,64,false));}
    public String status(){
        var diagnostics=DrillDiagnostics.capture(this);
        return diagnostics.stateText().getString()+": "+diagnostics.hint().getString();
    }
    private void ejectOutput(){if(level.getBlockEntity(at(-1,0,0)) instanceof HatchEntity hatch)hatch.ejectOutput();}
    /** Resolve a nearby controller for Jade, including misoriented ports and solid frame blocks. */
    public static DrillMachine controllerFor(Level level,BlockPos part){
        if(level.getBlockEntity(part) instanceof DrillMachine machine)return machine;
        for(int x=-2;x<=2;x++)for(int y=-1;y<=1;y++)for(int z=-2;z<=2;z++){
            var candidate=part.offset(x,y,z);
            if(level.hasChunkAt(candidate)&&level.getBlockEntity(candidate) instanceof DrillMachine m){
                Direction front=m.facingIssue!=null&&m.facingIssue.part()==DrillLayout.Part.CONTROLLER?m.facingIssue.expected():m.facing();
                for(int px=-1;px<=1;px++)for(int py=0;py<3;py++)for(int pz=-1;pz<=1;pz++)
                    if(m.tier().part(px,py,pz)!=DrillLayout.Part.AIR&&m.at(front,px,py,pz).equals(part))return m;
            }
        }
        return null;
    }
    private void setSpeed(float speed){if(workingSpeed!=speed){workingSpeed=speed;setChanged();sendUpdate();}}
    private void sendUpdate(){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private int coolantNeeded(float rpm){return (int)((coolantRemainder+Math.min(Math.abs(rpm),256)*WATER_PER_CYCLE)/tier().workPerItem);}
    private boolean cool(float rpm){
        if(tier()!=DrillTier.STEEL)return true;
        int needed=coolantNeeded(rpm);var tank=waterTank();
        if(tank==null||tank.water.getFluidAmount()<Math.max(1,needed)){
            dryTicks++;setChanged();
            if(dryTicks>=DRY_LIMIT){broken=true;work=0;setSpeed(0);sendUpdate();
                if(level instanceof net.minecraft.server.level.ServerLevel server){
                    var center=at(0,1,0);server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,center.getX()+.5,center.getY(),center.getZ()+.5,16,.4,.4,.4,.03);
                    level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.ANVIL_LAND,net.minecraft.sounds.SoundSource.BLOCKS,.7f,.6f);
                }
            }
            return false;
        }
        tank.water.drain(needed,net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        coolantRemainder=(coolantRemainder+Math.min(Math.abs(rpm),256)*WATER_PER_CYCLE)%tier().workPerItem;
        if(dryTicks!=0){dryTicks=0;sendUpdate();}return true;
    }
    public static void tick(Level l,BlockPos p,BlockState s,DrillMachine m){
        if(l.isClientSide){
            m.previousClientAngle=m.clientAngle;m.clientAngle+=m.workingSpeed*.3f;
            return;
        }
        // Validate before each production tick so breaking any structural block stops extraction immediately.
        if(!m.checkStructure()){m.setSpeed(0);return;}
        if(m.broken){m.setSpeed(0);return;}
        var drive=l.getBlockEntity(m.at(1,1,0));
        float rpm=drive instanceof DrillDrive d?d.getSpeed():0;
        if(rpm==0||Math.abs(rpm)<m.tier().minimumRpm){m.setSpeed(0);return;}
        // Stable next result prevents rerolling a blocked output.
        ItemStack result=m.nextOutput();
        if(!ItemHandlerHelper.insertItemStacked(m.inventory,result,true).isEmpty()){
            m.ejectOutput();
            if(!ItemHandlerHelper.insertItemStacked(m.inventory,result,true).isEmpty()){m.setSpeed(0);return;}
        }
        m.setSpeed(rpm);
        if(!m.cool(rpm))return; // Dry running spins but never produces free output.
        if(m.tier()==DrillTier.STEEL&&l.getGameTime()%3==0&&l instanceof net.minecraft.server.level.ServerLevel server){
            var center=m.at(0,0,0);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,center.getX()+.5,center.getY()+1.12,center.getZ()+.5,3,.55,.03,.55,0);
        }
        dk.revo.revolib.MachineFeedback.work(l,p,m.tier()==DrillTier.WOODEN?dk.revo.revolib.MachineFeedback.Voice.WOOD_DRILL:dk.revo.revolib.MachineFeedback.Voice.METAL_DRILL);
        dk.revo.revolib.MachineFeedback.particles(l,m.at(0,0,0),new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()),.15);
        m.work+=Math.min(Math.abs(rpm),256);
        if(m.work>=m.tier().workPerItem){m.work-=m.tier().workPerItem;ItemHandlerHelper.insertItemStacked(m.inventory,result,false);m.getPersistentData().putInt("cycle",m.getPersistentData().getInt("cycle")+1);m.ejectOutput();}
        m.setChanged();
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("Inventory",inventory.serializeNBT(registries));tag.putFloat("Work",work);tag.putFloat("Speed",workingSpeed);tag.putInt("DryTicks",dryTicks);tag.putBoolean("Broken",broken);tag.putFloat("CoolantRemainder",coolantRemainder);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);inventory.deserializeNBT(registries,tag.getCompound("Inventory"));work=tag.getFloat("Work");workingSpeed=tag.getFloat("Speed");dryTicks=Math.clamp(tag.getInt("DryTicks"),0,DRY_LIMIT);broken=tag.getBoolean("Broken");coolantRemainder=Math.clamp(tag.getFloat("CoolantRemainder"),0,tier().workPerItem-1);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
