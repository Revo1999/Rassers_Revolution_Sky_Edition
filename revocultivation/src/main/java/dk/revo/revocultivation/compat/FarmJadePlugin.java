package dk.revo.revocultivation.compat;
import dk.revo.revocultivation.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
@WailaPlugin(RevoCultivation.MOD_ID)
public class FarmJadePlugin implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration r){for(var c:new Class<?>[]{FarmPart.class,FarmController.class,FarmHatch.class,FarmWaterTank.class})r.registerBlockDataProvider(Provider.INSTANCE,c);}
    @Override public void registerClient(IWailaClientRegistration r){r.registerBlockComponent(Provider.INSTANCE,FarmWaterTank.class);r.registerBlockComponent(Provider.INSTANCE,FarmPart.class);r.registerBlockComponent(Provider.INSTANCE,FarmController.class);r.registerBlockComponent(Provider.INSTANCE,FarmHatch.class);}
    public enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
        INSTANCE;
        public ResourceLocation getUid(){return ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"first_tier_status");}
        public void appendServerData(CompoundTag tag,BlockAccessor a){var m=FarmMachine.find(a.getLevel(),a.getPosition());if(m!=null)tag.putIntArray("RevoFarm",new int[]{m.state()==7?0:m.state(),m.progress(),m.cycleTicks(),m.water.getFluidAmount(),m.buffered(),m.kind().ordinal(),m.energy(),m.energyCapacity()});}
        public void appendTooltip(ITooltip tip,BlockAccessor a,IPluginConfig config){
            var d=a.getServerData().getIntArray("RevoFarm");if(d.length!=8)return;var kind=MachineKind.values()[Math.clamp(d[5],0,MachineKind.values().length-1)];
            tip.add(Component.translatable("state.revocultivation."+d[0]).withStyle(d[0]==1?ChatFormatting.GREEN:ChatFormatting.YELLOW));
            tip.add(Component.translatable("gui.revocultivation.cycle",d[1]*100/Math.max(1,d[2]),MachineKind.seconds(d[2])));
            if(kind.needsWater())tip.add(Component.translatable("gui.revocultivation.water_capacity",d[3],kind.waterCapacity()));
            if(kind.electric())tip.add(Component.translatable("gui.revocultivation.energy",d[6],d[7],kind.energyPerTick()));
            if(a.getBlock() instanceof FarmHatch)tip.add(Component.translatable("gui.revocultivation.buffer",d[4]));
        }
    }
}
