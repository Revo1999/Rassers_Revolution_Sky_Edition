package dk.revo.revolib;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Successful work refreshes a client sound lease; idle/unloaded machines cannot leave orphan loops. */
@EventBusSubscriber(modid=RevoLib.MOD_ID)
public final class MachineFeedback {
    public enum Voice {
        WOOD_DRILL, METAL_DRILL, STEEL_DRILL, ELECTRIC_DRILL, IRRIGATION, GREENHOUSE,
        TREE_STEEL, UNDERWATER, CRYSTAL_GROWTH, HARVESTER, HARVESTER_STEEL,
        COMPOST, SILK, SLUICE, VACUUM, VACUUM_2, VACUUM_3, CRUCIBLE, RAIN,
        CONVERTER, CONVERTER_2, CONVERTER_3, STORAGE, FURNACE, FURNACE_TURBINE,
        FURNACE_HEAVY, FURNACE_CRYSTAL, FURNACE_RAINBOW, TRADE,
        INTERACT, TRANSFER, FUEL_FEED, VALVE, GRAVEL_FEED, HARVEST, CHOP,
        SAW, CRYSTAL_PICK, WIND, CRAFT, LIGHT_SWITCH, POUR, IGNITE, MOTOR_START, MOTOR_STOP;
        public boolean loop(){return switch(this){
            case COMPOST,SILK,TRADE,INTERACT,TRANSFER,FUEL_FEED,VALVE,GRAVEL_FEED,HARVEST,
                CHOP,SAW,CRYSTAL_PICK,WIND,CRAFT,LIGHT_SWITCH,POUR,IGNITE,MOTOR_START,MOTOR_STOP->false;
            default->true;
        };}
        public int interval(){return switch(this){case COMPOST,SILK->160;case TRADE->100;default->10;};}
        public float volume(){return switch(this){
            case COMPOST,SILK->.12f;
            case CONVERTER,CONVERTER_2,CONVERTER_3,IRRIGATION,GREENHOUSE,CRYSTAL_GROWTH->.18f;
            case INTERACT,LIGHT_SWITCH,TRANSFER,VALVE,CRAFT->.15f;
            default->.24f;
        };}
        public SoundEvent event(){return EVENTS.get(this).get();}
    }
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,RevoLib.MOD_ID);
    private static final Map<Voice,java.util.function.Supplier<SoundEvent>> EVENTS=new EnumMap<>(Voice.class);
    private static final Map<BlockEntity,EnumMap<Voice,Long>> LAST=new WeakHashMap<>();
    static {for(var voice:Voice.values()){
        var id=ResourceLocation.fromNamespaceAndPath(RevoLib.MOD_ID,"machine."+voice.name().toLowerCase(Locale.ROOT));
        EVENTS.put(voice,SOUNDS.register(id.getPath(),()->SoundEvent.createVariableRangeEvent(id)));
    }}
    private MachineFeedback(){}
    private static boolean ready(Level level,BlockPos pos,Voice voice,int interval){
        if(level==null||level.isClientSide)return false;
        var be=level.getBlockEntity(pos);if(be==null)return false;
        var times=LAST.computeIfAbsent(be,b->new EnumMap<>(Voice.class));
        long now=level.getGameTime(),previous=times.getOrDefault(voice,Long.MIN_VALUE/2);
        if(now>=previous&&now-previous<interval)return false;
        times.put(voice,now);return true;
    }
    public static void work(Level level,BlockPos pos,Voice voice){
        if(ready(level,pos,voice,voice.interval()))send(level,pos,voice);
    }
    public static void cue(Level level,BlockPos pos,Voice voice){
        int interval=switch(voice){case TRANSFER->40;case FUEL_FEED->60;case VALVE->100;case GRAVEL_FEED->30;default->8;};
        if(ready(level,pos,voice,interval))send(level,pos,voice);
    }
    public static void blockCue(Level level,BlockPos pos,Voice voice){
        if(level instanceof ServerLevel)send(level,pos,voice);
    }
    private static void send(Level level,BlockPos pos,Voice voice){
        PacketDistributor.sendToPlayersNear((ServerLevel)level,null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,20,new MachineSoundPayload(pos,voice.ordinal()));
    }
    public static void particles(Level level,BlockPos pos,ParticleOptions particle,double y){
        if(level instanceof ServerLevel server&&Math.floorMod(level.getGameTime()+pos.asLong(),8)==0)
            server.sendParticles(particle,pos.getX()+.5,pos.getY()+y,pos.getZ()+.5,1,.22,.04,.22,.01);
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event){
        if(event.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND)return;
        var be=event.getLevel().getBlockEntity(event.getPos());
        if(be!=null&&be.getClass().getName().startsWith("dk.revo."))cue(event.getLevel(),event.getPos(),Voice.INTERACT);
    }
}
