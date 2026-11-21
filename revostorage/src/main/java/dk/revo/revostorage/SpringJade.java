package dk.revo.revostorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

@WailaPlugin(RevoStorage.MOD_ID)
public final class SpringJade implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration r) { r.registerBlockDataProvider(Provider.INSTANCE, SpringBlock.class); }
    @Override public void registerClient(IWailaClientRegistration r) { r.registerBlockComponent(Provider.INSTANCE, SpringBlock.class); }

    private enum Provider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RevoStorage.MOD_ID, "spring_load");

        @Override public ResourceLocation getUid() { return ID; }

        @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof SpringEntity spring) {
                data.putInt("SpringCharge", spring.charge());
                data.putBoolean("ShaftWinding", spring.isShaftWinding());
            }
        }

        @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var data = accessor.getServerData();
            if (!data.contains("SpringCharge")) return;
            int charge = data.getInt("SpringCharge");
            var elements = IElementHelper.get();
            tooltip.add(elements.progress(charge / (float) SpringEntity.CAPACITY,
                    Component.translatable("jade.revostorage.spring_load", charge, SpringEntity.CAPACITY),
                    elements.progressStyle().color(0xffb67b42), BoxStyle.getNestedBox(), true));
            if (data.getBoolean("ShaftWinding")) tooltip.add(Component.translatable("jade.revostorage.spring_winding"));
        }
    }
}
