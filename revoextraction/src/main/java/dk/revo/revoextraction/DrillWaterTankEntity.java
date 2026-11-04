package dk.revo.revoextraction;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class DrillWaterTankEntity extends BlockEntity {
    public static final int CAPACITY=8000;
    public final FluidTank water=new FluidTank(CAPACITY,f->f.is(Fluids.WATER)){
        @Override protected void onContentsChanged(){
            setChanged();
            if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
    };
    public DrillWaterTankEntity(BlockPos p,BlockState s){super(RevoExtraction.WATER_TANK_ENTITY.get(),p,s);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("Water",water.writeToNBT(registries,new CompoundTag()));}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);water.readFromNBT(registries,tag.getCompound("Water"));}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
