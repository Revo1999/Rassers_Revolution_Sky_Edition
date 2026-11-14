package dk.revo.revolib;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Short positional work phrases, triggered by successful work, never by merely having power.
 * No detached looping sounds
 * survive a machine being removed/unloaded; the longest phrase tails off in under one second. */
@EventBusSubscriber(modid=RevoLib.MOD_ID)
public final class MachineFeedback {
    public enum Voice { WOOD_DRILL, METAL_DRILL, ELECTRIC_DRILL, IRRIGATION, HARVESTER,
        COMPOST, SILK, SLUICE, VACUUM, CRUCIBLE, RAIN, CONVERTER, STORAGE, FURNACE, TRADE, INTERACT }
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,RevoLib.MOD_ID);
    private static final Map<Voice,java.util.function.Supplier<SoundEvent>> EVENTS=new EnumMap<>(Voice.class);
    private static final Map<net.minecraft.world.level.block.entity.BlockEntity,Long> LAST=new WeakHashMap<>();
    static {for(var voice:Voice.values()){
        var id=ResourceLocation.fromNamespaceAndPath(RevoLib.MOD_ID,"machine."+voice.name().toLowerCase(Locale.ROOT));
        EVENTS.put(voice,SOUNDS.register(id.getPath(),()->SoundEvent.createVariableRangeEvent(id)));
    }}
    private MachineFeedback(){}
    public static void work(Level level,BlockPos pos,Voice voice){
        if(level==null||level.isClientSide)return;
        var be=level.getBlockEntity(pos);if(be==null)return;
        long now=level.getGameTime(),previous=LAST.getOrDefault(be,Long.MIN_VALUE/2);
        if(now>=previous&&now-previous<20)return;
        LAST.put(be,now);
        level.playSound(null,pos,EVENTS.get(voice).get(),SoundSource.BLOCKS,.22f,.97f+level.random.nextFloat()*.06f);
    }
    public static void particles(Level level,BlockPos pos,ParticleOptions particle,double y){
        if(level instanceof ServerLevel server&&Math.floorMod(level.getGameTime()+pos.asLong(),8)==0)
            server.sendParticles(particle,pos.getX()+.5,pos.getY()+y,pos.getZ()+.5,1,.22,.04,.22,.01);
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event){
        var level=event.getLevel();if(level.isClientSide||event.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND)return;
        var be=level.getBlockEntity(event.getPos());
        if(be!=null&&be.getClass().getName().startsWith("dk.revo."))
            level.playSound(null,event.getPos(),EVENTS.get(Voice.INTERACT).get(),SoundSource.BLOCKS,.18f,1);
    }
}
