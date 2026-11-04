package dk.revo.revocultivation.compat;
import dk.revo.revocultivation.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
@WailaPlugin(RevoCultivation.MOD_ID)
public class SmallMachineJade implements IWailaPlugin{
 public void register(IWailaCommonRegistration r){r.registerBlockDataProvider(Provider.INSTANCE,SmallMachineBlock.class);}
 public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,SmallMachineBlock.class);}
 enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
 INSTANCE;
 public ResourceLocation getUid(){return ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"small_cultivation_status");}
 public void appendServerData(CompoundTag t,BlockAccessor a){if(a.getBlockEntity() instanceof SmallMachineEntity m)t.putIntArray("RevoSmall",new int[]{m.visualStage()==3?5:m.status(),m.visualStage()==3?m.duration():m.progress(),m.duration()});}
 public void appendTooltip(ITooltip tip,BlockAccessor a,IPluginConfig c){var d=a.getServerData().getIntArray("RevoSmall");if(d.length!=3)return;tip.add(Component.translatable("small.revocultivation.status."+d[0]));tip.add(Component.literal(d[1]*100/Math.max(1,d[2])+"%"));}
 }
}

