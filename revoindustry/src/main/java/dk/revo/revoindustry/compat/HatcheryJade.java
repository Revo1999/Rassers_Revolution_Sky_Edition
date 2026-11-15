package dk.revo.revoindustry.compat;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.hatchery.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(RevoIndustry.MOD_ID)
public final class HatcheryJade implements IWailaPlugin {
    public void register(IWailaCommonRegistration r){r.registerBlockDataProvider(Provider.INSTANCE,HatcheryBlock.class);}
    public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,HatcheryBlock.class);}
    enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
        INSTANCE;
        public ResourceLocation getUid(){return Hatchery.id("wither_hatchery");}
        public void appendServerData(CompoundTag tag,BlockAccessor accessor){
            var be=accessor.getBlockEntity();var c=be instanceof HatcheryController controller?controller:be instanceof HatcheryPort port?port.controller():null;
            if(c!=null)tag.putIntArray("RevoHatchery",new int[]{c.stage(),c.progress(),c.stageTicks(),c.energy(),c.status()});
        }
        public void appendTooltip(ITooltip tooltip,BlockAccessor accessor,IPluginConfig config){
            var v=accessor.getServerData().getIntArray("RevoHatchery");if(v.length!=5)return;
            String state=switch(v[4]){case 0->"Incomplete structure";case 1->"Waiting for egg";case 2->"Insufficient power";case 3->"Output full";case 4->"Wither spawn blocked";default->v[0]==1?"Incubating":v[0]==2?"Hatching":"Neutralizing";};
            tooltip.add(Component.literal(state+(v[2]>0?" | "+v[1]*100/v[2]+"%":"")));tooltip.add(Component.literal(v[3]+" EU | 8192 EU/t"));
        }
    }
}
