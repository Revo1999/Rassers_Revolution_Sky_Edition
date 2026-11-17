package dk.revo.revolib;

import net.neoforged.fml.common.Mod;

/** Development entry point. Gameplay is implemented in later milestones. */
@Mod(RevoLib.MOD_ID)
public final class RevoLib {
    public static final String MOD_ID = "revolib";
    public RevoLib(net.neoforged.bus.api.IEventBus bus,net.neoforged.fml.ModContainer container) {
        MachineFeedback.SOUNDS.register(bus);bus.addListener(MachineSoundPayload::register);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,MachineAudioConfig.SPEC);
    }
}
