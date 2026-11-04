package dk.revo.revoextraction.compat;

import java.util.List;
import dk.revo.revoextraction.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Jade discovers this only when installed. Machine values always come from the server. */
@WailaPlugin(RevoExtraction.MOD_ID)
public final class DrillJadePlugin implements IWailaPlugin {
    private static final List<Class<? extends Block>> BLOCKS=List.of(DrillPart.class,DrillController.class,DrillHatch.class,DrillInput.class,DrillWaterTank.class);
    @Override public void register(IWailaCommonRegistration r){for(var block:BLOCKS)r.registerBlockDataProvider(Provider.INSTANCE,block);}
    @Override public void registerClient(IWailaClientRegistration r){for(var block:BLOCKS)r.registerBlockComponent(Provider.INSTANCE,block);}
    public enum Provider implements IServerDataProvider<BlockAccessor>,IBlockComponentProvider {
        INSTANCE;
        private static final ResourceLocation UID=ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"drill_diagnostics");
        @Override public ResourceLocation getUid(){return UID;}
        @Override public void appendServerData(CompoundTag data,BlockAccessor accessor){
            var machine=DrillMachine.controllerFor(accessor.getLevel(),accessor.getPosition());
            if(machine==null)return;
            var diagnostics=DrillDiagnostics.capture(machine);
            // Jade stays compact. Facing corrections belong in the controller window.
            data.putIntArray("RevoDrill",new int[]{diagnostics.progress(),diagnostics.rpm(),diagnostics.state()==5?0:diagnostics.state()});
            int items=0;for(int i=0;i<machine.inventory.getSlots();i++)items+=machine.inventory.getStackInSlot(i).getCount();
            data.putInt("RevoOutputCount",items);
            if(machine.tier()==DrillTier.STEEL)data.putInt("RevoWater",machine.waterAmount());
        }
        @Override public void appendTooltip(ITooltip tooltip,BlockAccessor accessor,IPluginConfig config){
            int[] values=accessor.getServerData().getIntArray("RevoDrill");
            if(values.length==3){
                var status=DrillDiagnostics.read(i->i<3?values[i]:0);
                tooltip.add(status.stateText().copy().withStyle(status.state()==1?ChatFormatting.GREEN:ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("gui.revoextraction.rpm",status.rpm()));
                tooltip.add(Component.translatable("gui.revoextraction.cycle",Math.clamp(status.progress()*100/1200,0,100)));
                if(accessor.getServerData().contains("RevoWater"))tooltip.add(Component.translatable("gui.revoextraction.water",accessor.getServerData().getInt("RevoWater"),DrillWaterTankEntity.CAPACITY));
                if(accessor.getBlock() instanceof DrillHatch){
                    tooltip.add(Component.translatable("jade.revoextraction.output_count",accessor.getServerData().getInt("RevoOutputCount")));
                    tooltip.add(Component.translatable("jade.revoextraction.auto_output"));
                }
            }else tooltip.add(Component.translatable("jade.revoextraction.unlinked"));
        }
    }
}
