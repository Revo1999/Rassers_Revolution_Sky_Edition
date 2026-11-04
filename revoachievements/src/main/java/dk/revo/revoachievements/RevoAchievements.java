package dk.revo.revoachievements;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(RevoAchievements.MOD_ID)
public final class RevoAchievements {
    public static final String MOD_ID="revoachievements";
    public static final Logger LOGGER=LogUtils.getLogger();
    public static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS=net.neoforged.neoforge.registries.DeferredRegister.createItems(MOD_ID);
    public RevoAchievements(IEventBus bus) {
        if(ModList.get().isLoaded("simplehats")){
            // These shipped Simple Hats models have no registered items in 0.4.1.
            // Reuse their installed assets, without copying ARR art or rewriting its config.
            ITEMS.registerSimpleItem("off_duty_cap",new net.minecraft.world.item.Item.Properties().stacksTo(1));
            ITEMS.registerSimpleItem("miner_helmet",new net.minecraft.world.item.Item.Properties().stacksTo(1));
        }
        ITEMS.register(bus);
        bus.addListener(WardrobeNetwork::register);bus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(new WardrobeEvents());
    }
    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            if(ModList.get().isLoaded("ftbquests"))dk.revo.revoachievements.compat.FtbCosmeticRewards.register();
            if(ModList.get().isLoaded("playernamestyler"))dk.revo.revoachievements.compat.TitleStyler.register();
        });
    }
}
