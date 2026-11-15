package dk.revo.revoindustry.hatchery;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class HatcheryController extends BlockEntity implements MenuProvider {
    // Stages: idle, incubating, hatching, neutralizing. A star only follows a lethal real-entity hit.
    private int stage,progress,energy,status;
    private UUID job,witherId;
    private boolean formed,exporting;
    private String problem="Incomplete structure";
    private long lastTick=Long.MIN_VALUE;
    public HatcheryController(BlockPos pos,BlockState state){super(Hatchery.CONTROLLER_ENTITY.get(),pos,state);}
    public Direction facing(){return getBlockState().getValue(HatcheryBlock.FACING);}
    public UUID jobId(){return job;}
    public int stage(){return stage;}
    public int progress(){return progress;}
    public int energy(){return energy;}
    public int status(){return status;}
    public String problem(){return problem;}
    public boolean expectsWither(UUID id){return stage>=2&&Objects.equals(witherId,id);}
    public int stageTicks(){return switch(stage){case 1->Hatchery.INCUBATION_TICKS;case 2->Hatchery.HATCH_TICKS;case 3->Hatchery.KILL_TICKS;default->0;};}
    public BlockPos at(int x,int y,int z){return HatcheryLayout.at(worldPosition,facing(),x,y,z);}
    public HatcheryPort input(){return port(-2,0,2);}
    public HatcheryPort output(){return port(2,0,2);}
    private HatcheryPort port(int x,int y,int z){var p=at(x,y,z);return level!=null&&level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof HatcheryPort port?port:null;}
    public boolean validate(){
        if(level==null)return false;
        for(var cell:HatcheryLayout.SHELL){
            var p=HatcheryLayout.at(worldPosition,facing(),cell.offset());
            if(!level.hasChunkAt(p)){problem="Structure chunk not loaded";return formed=false;}
            var s=level.getBlockState(p);
            if(!s.is(HatcheryLayout.block(cell.part()))){problem="Missing "+cell.part().name().toLowerCase(Locale.ROOT)+" at "+cell.offset().toShortString();return formed=false;}
            if(HatcheryLayout.directional(cell.part())&&s.getValue(HatcheryBlock.FACING)!=HatcheryLayout.rotate(cell.facing(),facing())){problem="Wrong facing: "+cell.part().name().toLowerCase(Locale.ROOT)+" at "+cell.offset().toShortString();return formed=false;}
        }
        for(int y=1;y<=3;y++)for(int z=1;z<=3;z++)for(int x=-1;x<=1;x++)if(!level.getBlockState(at(x,y,z)).isAir()){problem="Clear interior at "+x+", "+y+", "+z;return formed=false;}
        problem="Structure complete";return formed=true;
    }
    public int receiveEu(long amount,boolean simulate){
        if(amount<=0||!validate())return 0;
        int accepted=(int)Math.min(amount,Hatchery.BUFFER_EU-energy);
        if(!simulate&&accepted>0){energy+=accepted;setChanged();}
        return accepted;
    }
    public void tick(){
        if(!(level instanceof ServerLevel server))return;
        // Cap processing to one step per world tick, even under capability reentrancy.
        if(lastTick==level.getGameTime())return;lastTick=level.getGameTime();
        if(!validate()){status=0;lights(false);sync();return;}
        export();
        var output=output();var input=input();
        if(output==null||input==null){status=0;lights(false);sync();return;}
        if(!output.canAcceptStar()){status=3;lights(false);sync();return;}
        if(stage==0){
            if(!input.hasEgg()){status=1;lights(false);sync();return;}
            if(energy<Hatchery.EU_PER_TICK){status=2;lights(false);sync();return;}
            input.takeEgg();stage=1;progress=0;job=UUID.randomUUID();witherId=null;setChanged();
        }
        if(energy<Hatchery.EU_PER_TICK){status=2;lights(false);sync();return;}
        ChamberWither wither=null;
        if(stage>=2){
            var existing=witherId==null?null:server.getEntity(witherId);
            if(existing instanceof ChamberWither w&&w.belongsTo(this)&&w.isAlive())wither=w;
            else {
                // All interior chunks are loaded. Recreate only the missing owned actor, never an output.
                wither=spawnWither(server);
                if(wither==null){status=4;lights(false);sync();return;}
                if(stage==3)wither.setHealth(Math.max(1,300-(progress/20)*30));
            }
            wither.anchor(this);
        }
        energy-=Hatchery.EU_PER_TICK;progress++;status=5;lights((progress/8)%2==0);
        if(stage==1){
            if(progress%40==0)server.playSound(null,at(0,1,2),SoundEvents.BEACON_AMBIENT,SoundSource.BLOCKS,.25f,.65f+progress/(float)Hatchery.INCUBATION_TICKS);
            if(progress>=Hatchery.INCUBATION_TICKS){
                wither=spawnWither(server);
                if(wither!=null){stage=2;progress=0;wither.setHatchScale(.05);server.sendParticles(ParticleTypes.SOUL,at(0,1,2).getX()+.5,worldPosition.getY()+1.5,at(0,1,2).getZ()+.5,45,.6,.5,.6,.015);server.playSound(null,at(0,1,2),SoundEvents.TURTLE_EGG_HATCH,SoundSource.BLOCKS,1,.6f);}
                else progress=Hatchery.INCUBATION_TICKS-1;
            }
        } else if(stage==2){
            wither.setHatchScale(.05+.70*progress/Hatchery.HATCH_TICKS);
            if(progress>=Hatchery.HATCH_TICKS){stage=3;progress=0;wither.setHatchScale(.75);}
        } else if(stage==3&&progress%20==0){
            wither.setHatchScale(.75);
            wither.machineHit(this,30);
            server.sendParticles(ParticleTypes.DAMAGE_INDICATOR,wither.getX(),wither.getY()+1.5,wither.getZ(),8,.3,.7,.3,.02);
            if(wither.isDeadOrDying()){
                // Same server tick: one consumed egg, one lethal hit, one saved result. No world loot duplicate.
                output.commitStar();stage=0;progress=0;job=null;witherId=null;
                server.playSound(null,worldPosition,SoundEvents.BEACON_ACTIVATE,SoundSource.BLOCKS,.5f,1.4f);
                export();
            }
        }
        setChanged();sync();
    }
    private ChamberWither spawnWither(ServerLevel server){
        var wither=Hatchery.WITHER.get().create(server);if(wither==null)return null;
        wither.bind(this);wither.setHatchScale(stage==3?.75:.05);
        if(!server.addFreshEntity(wither))return null;
        witherId=wither.getUUID();setChanged();return wither;
    }
    public void export(){
        if(exporting||level==null)return;var out=output();if(out==null)return;
        exporting=true;try{out.export();}finally{exporting=false;}
    }
    private static final List<HatcheryLayout.Cell> LIGHT_CELLS=HatcheryLayout.SHELL.stream().filter(c->c.part()==Hatchery.Part.LIGHT||c.part()==Hatchery.Part.CONTROLLER||c.part()==Hatchery.Part.CASING&&c.offset().getY()==4&&c.offset().getZ()==2&&Math.abs(c.offset().getX())<=1).toList();
    private void lights(boolean on){
        if(level==null)return;
        for(var c:LIGHT_CELLS){
            boolean roof=c.part()==Hatchery.Part.CASING&&c.offset().getY()==4&&c.offset().getZ()==2&&Math.abs(c.offset().getX())<=1;
            if(c.part()!=Hatchery.Part.LIGHT&&c.part()!=Hatchery.Part.CONTROLLER&&!roof)continue;
            var p=HatcheryLayout.at(worldPosition,facing(),c.offset());if(!level.hasChunkAt(p))continue;
            boolean lampOn=roof?stage>0&&status==5&&formed:on;
            var s=level.getBlockState(p);if(s.is(HatcheryLayout.block(c.part()))&&s.getValue(HatcheryBlock.ACTIVE)!=lampOn)level.setBlock(p,s.setValue(HatcheryBlock.ACTIVE,lampOn),3);
        }
    }
    public void breakMachine(){
        if(level==null||level.isClientSide)return;
        if(witherId!=null&&level instanceof ServerLevel server&&server.getEntity(witherId) instanceof ChamberWither w)w.discard();
        if(stage!=0)Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,new ItemStack(Hatchery.EGG.get()));
        stage=0;progress=0;job=null;witherId=null;energy=0;lights(false);setChanged();
    }
    private int syncStage=-1,syncProgress=-1,syncStatus=-1;
    private void sync(){if(syncStage!=stage||syncStatus!=status||Math.abs(syncProgress-progress)>=5){syncStage=stage;syncProgress=progress;syncStatus=status;level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
    protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.putInt("Stage",stage);t.putInt("Progress",progress);t.putInt("Energy",energy);if(job!=null)t.putUUID("Job",job);if(witherId!=null)t.putUUID("Wither",witherId);}
    protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);stage=Math.clamp(t.getInt("Stage"),0,3);progress=Math.clamp(t.getInt("Progress"),0,stageTicks());energy=Math.clamp(t.getInt("Energy"),0,Hatchery.BUFFER_EU);job=t.hasUUID("Job")?t.getUUID("Job"):null;witherId=t.hasUUID("Wither")?t.getUUID("Wither"):null;if(stage!=0&&job==null){stage=0;progress=0;}if(t.contains("Status")){status=t.getInt("Status");problem=t.getString("Problem");}}
    public CompoundTag getUpdateTag(HolderLookup.Provider p){var t=super.getUpdateTag(p);saveAdditional(t,p);t.putInt("Status",status);t.putString("Problem",problem);return t;}
    public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public void handleUpdateTag(CompoundTag t,HolderLookup.Provider p){loadAdditional(t,p);status=t.getInt("Status");problem=t.getString("Problem");}
    public Component getDisplayName(){return Component.translatable("block.revoindustry.wither_hatchery");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new HatcheryMenu(id,inv,this);}
}
