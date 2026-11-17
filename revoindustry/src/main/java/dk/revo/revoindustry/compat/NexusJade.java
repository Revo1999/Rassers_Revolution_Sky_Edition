package dk.revo.revoindustry.compat;

import dk.revo.revoindustry.nexus.*;
import aztech.modern_industrialization.machines.MachineBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin("revoindustry")
public final class NexusJade implements IWailaPlugin {
    public void register(IWailaCommonRegistration r){r.registerBlockDataProvider(Provider.INSTANCE,MachineBlock.class);}
    public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,MachineBlock.class);}
    enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
        INSTANCE;
        public ResourceLocation getUid(){return Nexus.id("nexus_status");}
        public void appendServerData(CompoundTag tag,BlockAccessor accessor){if(accessor.getBlockEntity() instanceof NexusMachine machine)tag.putString("RevoNexusStatus",machine.status());}
        public void appendTooltip(ITooltip tooltip,BlockAccessor accessor,IPluginConfig config){String status=accessor.getServerData().getString("RevoNexusStatus");if(!status.isEmpty())tooltip.add(Component.literal(status));}
    }
}
