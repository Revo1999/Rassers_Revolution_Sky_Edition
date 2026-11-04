package dk.revo.revoindustry.compat;
import dk.revo.revoindustry.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
@WailaPlugin(RevoIndustry.MOD_ID)
public class PrimitiveJade implements IWailaPlugin{
 public void register(IWailaCommonRegistration r){r.registerBlockDataProvider(Provider.INSTANCE,PrimitiveVesselBlock.class);r.registerBlockDataProvider(Converter.INSTANCE,PowerConverterBlock.class);}
 public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,PrimitiveVesselBlock.class);r.registerBlockComponent(Converter.INSTANCE,PowerConverterBlock.class);}
 enum Converter implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider{
  INSTANCE;
  public ResourceLocation getUid(){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,"power_converter");}
  public void appendServerData(CompoundTag t,BlockAccessor a){if(a.getBlockEntity() instanceof PowerConverterEntity m)t.putIntArray("RevoPower",new int[]{m.rateEu(),m.power.ratio(),m.power.feToEu()?0:1});}
  public void appendTooltip(ITooltip tip,BlockAccessor a,IPluginConfig c){var d=a.getServerData().getIntArray("RevoPower");if(d.length!=3)return;tip.add(Component.literal(d[2]==0?"FE -> EU":"EU -> FE"));tip.add(Component.literal(d[0]+" EU/t / "+d[0]*d[1]+" FE/t"));}
 }
 enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider{
 INSTANCE;
 public ResourceLocation getUid(){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,"primitive_vessel");}
 public void appendServerData(CompoundTag t,BlockAccessor a){if(a.getBlockEntity() instanceof PrimitiveVesselEntity m)t.putIntArray("RevoVessel",new int[]{m.status(),m.tank.getFluidAmount(),m.progress(),m.rain()?0:1});}
 public void appendTooltip(ITooltip tip,BlockAccessor a,IPluginConfig c){var d=a.getServerData().getIntArray("RevoVessel");if(d.length!=4)return;tip.add(Component.translatable("state.revoindustry."+d[0]));tip.add(Component.literal(d[1]+"/1000 mB"));if(d[3]==1&&d[0]==0)tip.add(Component.literal(d[2]*100/200+"%"));}
 }
}

