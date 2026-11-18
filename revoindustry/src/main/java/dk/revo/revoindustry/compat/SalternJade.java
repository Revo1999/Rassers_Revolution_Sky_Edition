package dk.revo.revoindustry.compat;
import dk.revo.revoindustry.saltern.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
@WailaPlugin("revoindustry")
public final class SalternJade implements IWailaPlugin {
 public void register(IWailaCommonRegistration r){r.registerBlockDataProvider(Provider.INSTANCE,SalternBlock.class);}public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,SalternBlock.class);}
 enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
  INSTANCE;public ResourceLocation getUid(){return Saltern.id("salt_geode");}
  public void appendServerData(CompoundTag t,BlockAccessor a){var be=a.getBlockEntity();var c=be instanceof SalternController ctrl?ctrl:be instanceof SalternPort p?p.controller():null;if(c!=null)t.putIntArray("RevoGeode",new int[]{c.status(),c.progress(),c.water.getFluidAmount(),c.brine.getFluidAmount(),c.energy()});}
  public void appendTooltip(ITooltip tooltip,BlockAccessor a,IPluginConfig config){var v=a.getServerData().getIntArray("RevoGeode");if(v.length!=5)return;tooltip.add(Component.literal(switch(v[0]){case 0->"Check structure";case 1->"Needs seawater";case 2->"Needs electricity";case 3->"Brine output full";default->v[1]<330?"Growing crystals":"Harvesting submerged crystals";}));tooltip.add(Component.literal(v[1]*100/400+"% | "+v[4]+" EU | 16 EU/t"));tooltip.add(Component.literal("Seawater "+v[2]+" / Brine "+v[3]+" mB"));}
 }
}
