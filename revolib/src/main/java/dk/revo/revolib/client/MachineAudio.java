package dk.revo.revolib.client;

import dk.revo.revolib.*;
import dk.revo.revolib.MachineFeedback.Voice;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Quiet positional loops with leases, fade envelopes and a nearby-room voice budget. */
@EventBusSubscriber(modid=RevoLib.MOD_ID,value=Dist.CLIENT)
public final class MachineAudio {
    private static final Map<BlockPos,Loop> LOOPS=new HashMap<>();
    private static ClientLevel world;
    private static long lastCue=Long.MIN_VALUE/2;
    private MachineAudio(){}
    public static int activeLoops(){return LOOPS.size();}
    public static java.util.List<String> profiles(){return LOOPS.values().stream().map(l->l.voice.name()).sorted().toList();}
    public static int playingLoops(){return (int)LOOPS.values().stream().filter(l->Minecraft.getInstance().getSoundManager().isActive(l)).count();}
    private static boolean furnace(Voice v){return v.name().startsWith("FURNACE");}
    private static boolean motor(Voice v){return furnace(v)||v.name().endsWith("DRILL")||v==Voice.STORAGE;}
    private static void transition(BlockPos pos,Voice voice){
        var mc=Minecraft.getInstance();mc.getSoundManager().play(new SimpleSoundInstance(voice.event(),SoundSource.BLOCKS,
            .08f*MachineAudioConfig.VOLUME.get().floatValue()/(float)Math.sqrt(Math.max(1,LOOPS.size())),1,RandomSource.create(),pos));
    }
    public static void receive(MachineSoundPayload packet){
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||packet.voice()<0||packet.voice()>=Voice.values().length)return;
        if(world!=mc.level)clear(mc);
        var pos=packet.pos();if(mc.player.distanceToSqr(pos.getCenter())>144||!mc.level.hasChunkAt(pos))return;
        var voice=Voice.values()[packet.voice()];long now=mc.level.getGameTime();
        if(MachineAudioConfig.VOLUME.get()<=0||mc.options.getSoundSourceVolume(SoundSource.BLOCKS)<=0||mc.options.getSoundSourceVolume(SoundSource.MASTER)<=0)return;
        if(!voice.loop()){
            if(now-lastCue<4&&now>=lastCue)return;
            lastCue=now;
            mc.getSoundManager().play(new SimpleSoundInstance(voice.event(),SoundSource.BLOCKS,
                voice.volume()*MachineAudioConfig.VOLUME.get().floatValue()/(float)Math.sqrt(Math.max(1,LOOPS.size())),
                .98f+mc.level.random.nextFloat()*.04f,RandomSource.create(),pos));return;
        }
        var existing=LOOPS.get(pos);
        if(existing!=null&&existing.voice==voice&&!existing.isStopped()){existing.lastWork=now;return;}
        if(existing!=null){existing.stopNow();LOOPS.remove(pos);}
        if(LOOPS.size()>=MachineAudioConfig.MAX_LOOPS.get()){
            var far=LOOPS.values().stream().max(Comparator.comparingDouble(l->mc.player.distanceToSqr(l.pos.getCenter()))).orElseThrow();
            if(mc.player.distanceToSqr(pos.getCenter())+9>=mc.player.distanceToSqr(far.pos.getCenter()))return;
            far.stopNow();LOOPS.remove(far.pos);
        }
        var loop=new Loop(mc.level,pos,voice,now);LOOPS.put(pos.immutable(),loop);mc.getSoundManager().play(loop);if(motor(voice))transition(pos,furnace(voice)?Voice.IGNITE:Voice.MOTOR_START);
    }
    private static void clear(Minecraft mc){
        for(var loop:LOOPS.values()){loop.stopNow();mc.getSoundManager().stop(loop);}
        LOOPS.clear();world=mc.level;lastCue=Long.MIN_VALUE/2;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(world!=mc.level){clear(mc);return;}
        LOOPS.values().removeIf(loop->{
            if(!loop.isStopped()&&world.getGameTime()-loop.created>10&&!mc.getSoundManager().isActive(loop))loop.stopNow();
            return loop.isStopped();
        });
    }
    private static final class Loop extends AbstractTickableSoundInstance {
        final ClientLevel level;final BlockPos pos;final Voice voice;final long created;long lastWork;float envelope;
        Loop(ClientLevel level,BlockPos pos,Voice voice,long now){
            super(voice.event(),SoundSource.BLOCKS,RandomSource.create());this.level=level;this.pos=pos.immutable();this.voice=voice;created=now;lastWork=now;
            x=pos.getX()+.5;y=pos.getY()+.5;z=pos.getZ()+.5;looping=true;delay=0;volume=.0001f;pitch=1;
        }
        @Override public boolean canStartSilent(){return true;}
        void stopNow(){stop();}
        @Override public void tick(){
            var mc=Minecraft.getInstance();
            if(mc.level!=level||mc.player==null||!level.hasChunkAt(pos)||level.getBlockEntity(pos)==null){stop();return;}
            boolean active=level.getGameTime()-lastWork<=26&&mc.player.distanceToSqr(pos.getCenter())<=196;
            envelope=Mth.approach(envelope,active?1:0,active?.10f:.16f);
            float crowd=1f/(float)Math.sqrt(Math.max(1,LOOPS.size()));
            volume=voice.volume()*MachineAudioConfig.VOLUME.get().floatValue()*crowd*envelope;
            if(!active&&envelope<=0){if(motor(voice)&&mc.player.distanceToSqr(pos.getCenter())<=144)transition(pos,Voice.MOTOR_STOP);stop();}
        }
    }
}
