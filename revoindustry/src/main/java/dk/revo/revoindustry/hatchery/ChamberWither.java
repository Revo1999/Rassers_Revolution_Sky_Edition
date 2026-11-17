package dk.revo.revoindustry.hatchery;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;

/** A real world WitherBoss, with containment enforced on both sides of every save/load. */
public final class ChamberWither extends WitherBoss {
    private BlockPos chamber=BlockPos.ZERO;
    private UUID owner;
    private boolean machineDamage;
    public ChamberWither(EntityType<? extends WitherBoss> type,Level level){super(type,level);setNoAi(true);setNoGravity(true);setPersistenceRequired();setInvulnerableTicks(0);xpReward=0;}
    public void bind(HatcheryController controller){chamber=controller.getBlockPos();owner=controller.jobId();anchor(controller);}
    public boolean belongsTo(HatcheryController c){return chamber.equals(c.getBlockPos())&&owner!=null&&owner.equals(c.jobId());}
    public void anchor(HatcheryController c){
        var p=HatcheryLayout.at(chamber,c.facing(),0,1,2);
        moveTo(p.getX()+.5,p.getY()+.05,p.getZ()+.5,c.facing().toYRot(),0);setDeltaMovement(0,0,0);
        yBodyRot=c.facing().toYRot();setYHeadRot(yBodyRot);setNoAi(true);setNoGravity(true);setInvulnerableTicks(0);
    }
    public void setHatchScale(double scale){var a=getAttribute(Attributes.SCALE);if(a!=null)a.setBaseValue(Math.clamp(scale,.05,.75));}
    public boolean machineHit(HatcheryController c,float damage){
        if(!belongsTo(c)||!isAlive())return false;
        machineDamage=true;try{invulnerableTime=0;return super.hurt(damageSources().genericKill(),damage);}finally{machineDamage=false;}
    }
    @Override public boolean hurt(DamageSource source,float amount){return machineDamage&&super.hurt(source,amount);}
    @Override protected void customServerAiStep(){} // No spawn explosion, healing, skull firing or terrain destruction.
    @Override public void performRangedAttack(LivingEntity target,float distance){}
    @Override protected boolean shouldDespawnInPeaceful(){return false;}
    @Override public boolean canChangeDimensions(Level from,Level to){return false;}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentHit){} // Result is committed once to the machine's saved output.
    @Override protected void dropAllDeathLoot(ServerLevel level,DamageSource source){}
    @Override public void tick(){
        if(!level().isClientSide&&!isDeadOrDying()){
            if(!level().hasChunkAt(chamber)){setDeltaMovement(0,0,0);return;}
            if(!(level().getBlockEntity(chamber) instanceof HatcheryController c)||!belongsTo(c)||!c.expectsWither(getUUID())){discard();return;}
            anchor(c);
        }
        super.tick();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putLong("RevoChamber",chamber.asLong());if(owner!=null)tag.putUUID("RevoJob",owner);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);chamber=BlockPos.of(tag.getLong("RevoChamber"));owner=tag.hasUUID("RevoJob")?tag.getUUID("RevoJob"):null;setNoAi(true);setNoGravity(true);setInvulnerableTicks(0);xpReward=0;}
}
