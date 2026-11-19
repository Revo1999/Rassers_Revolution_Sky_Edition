package dk.revo.revoutil;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(RevoUtil.MOD_ID)
public final class RevoUtil {
    public static final String MOD_ID = "revoutil";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String PACK_VERSION = readPackVersion();

    public RevoUtil(IEventBus bus) {
        var status = new PlaytestStatus();
        NeoForge.EVENT_BUS.addListener(new PlaytestCommands(status)::register);
        NeoForge.EVENT_BUS.addListener(status::tick);
        NeoForge.EVENT_BUS.addListener(status::logout);
        NeoForge.EVENT_BUS.addListener(status::stopped);
        bus.addListener(PlaytestStatus::registerPayload);
    }

    private static String readPackVersion() {
        try (var stream = RevoUtil.class.getResourceAsStream("/revoutil/version.txt")) {
            if (stream != null) {
                String version = new String(stream.readAllBytes(), StandardCharsets.UTF_8).strip();
                if (!version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not read the bundled pack version", exception);
        }
        return "unknown";
    }
}
