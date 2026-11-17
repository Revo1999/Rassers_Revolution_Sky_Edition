package dk.revo.revolib;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MachineAudioConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue VOLUME;
    public static final ModConfigSpec.IntValue MAX_LOOPS;
    static {
        var b=new ModConfigSpec.Builder();
        VOLUME=b.comment("Machine sound multiplier, in addition to Minecraft's Blocks volume. Zero mutes Revo machine audio.").defineInRange("machineVolume",.65,0,1);
        MAX_LOOPS=b.comment("Maximum simultaneous nearby machine loops; quieter mixing applies as more machines run.").defineInRange("maximumMachineLoops",8,1,16);
        SPEC=b.build();
    }
    private MachineAudioConfig(){}
}
