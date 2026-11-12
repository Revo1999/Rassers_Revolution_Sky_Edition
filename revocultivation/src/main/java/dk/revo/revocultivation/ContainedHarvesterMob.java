package dk.revo.revocultivation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** A real vanilla Mob owned and saved by the machine, never registered as a roaming entity.
 * Like a spawner display entity, it uses Minecraft's entity renderer without world AI,
 * collision, damage, drops, daylight burning, peaceful removal or despawn ticking. */
public final class ContainedHarvesterMob {
    private CompoundTag saved=new CompoundTag();
    private Mob mob;

    public static EntityType<? extends Mob> type(int program){
        return switch(program){case 20->EntityType.ZOMBIE;case 21->EntityType.SKELETON;case 22->EntityType.SPIDER;case 23->EntityType.COW;case 24->EntityType.BLAZE;case 25->EntityType.SLIME;case 26->EntityType.ENDERMAN;case 27->EntityType.MAGMA_CUBE;default->null;};
    }
    public Mob get(Level level,BlockPos bed,int program){
        var type=type(program);if(level==null||type==null)return null;
        if(mob==null||mob.level()!=level||mob.getType()!=type){
            mob=null;
            // Only the selected allow-listed species can be restored. Never load arbitrary
            // entity IDs or passengers from externally edited machine data.
            if(saved.getString("id").equals(EntityType.getKey(type).toString())){
                var data=saved.copy();data.remove("Passengers");
                var restored=EntityType.loadEntityRecursive(data,level,e->e);
                if(restored instanceof Mob m&&m.getType()==type)mob=m;
            }
            if(mob==null){
                mob=type.create(level);
                if(mob==null)return null;
                if(type==EntityType.SKELETON)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
            }
            mob.setPersistenceRequired();mob.setNoAi(true);mob.setNoGravity(true);
            mob.setInvulnerable(true);mob.setSilent(true);mob.setCanPickUpLoot(false);
            mob.setTarget(null);mob.setDeltaMovement(0,0,0);mob.clearFire();
            mob.setYRot(180);mob.yBodyRot=mob.yBodyRotO=180;mob.yHeadRot=mob.yHeadRotO=180;
            saved=save();
        }
        mob.setPos(bed.getX()+.5,bed.getY()+5d/16,bed.getZ()+.5);
        return mob;
    }
    public CompoundTag save(){
        if(mob!=null){var tag=mob.saveWithoutId(new CompoundTag());tag.putString("id",EntityType.getKey(mob.getType()).toString());tag.remove("Passengers");return tag;}
        return saved.copy();
    }
    public void load(CompoundTag tag){
        saved=tag.copy();
        // Routine block updates must not allocate another mob or reset its idle animation.
        if(mob!=null&&(!saved.hasUUID("UUID")||!saved.getUUID("UUID").equals(mob.getUUID())||!saved.getString("id").equals(EntityType.getKey(mob.getType()).toString())))mob=null;
    }
    public void animate(long time,boolean working){
        if(mob==null)return;
        // Animation clocks only: never invoke Mob.tick()/aiStep()/checkDespawn().
        mob.tickCount++;mob.yHeadRotO=mob.yHeadRot;
        mob.yHeadRot=180+(float)Math.sin(time*.025)*12;
        mob.walkAnimation.update(working?.12f:0,.2f);
    }
}
