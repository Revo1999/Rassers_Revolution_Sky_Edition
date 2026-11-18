package dk.revo.revoindustry.nexus;

import java.util.function.BiFunction;
import aztech.modern_industrialization.machines.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** MI's base block has no block-event forwarding; completion flashes need that client route. */
public final class NexusControllerBlock extends MachineBlock {
    public NexusControllerBlock(BiFunction<BlockPos,BlockState,? extends MachineBlockEntity> factory,Properties properties){super(factory,properties);}
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){if(level.getBlockEntity(pos) instanceof NexusMachine m&&m.construction!=null){if(!level.isClientSide)player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,p)->new NexusBuilderMenu(id,inv,m),net.minecraft.network.chat.Component.literal("Nexus Builder")),buffer->buffer.writeBlockPos(pos));return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);}return super.useWithoutItem(state,level,pos,player,hit);}
    @Override public boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int value){
        if(level.getBlockEntity(pos) instanceof NexusMachine machine)return machine.triggerEvent(id,value);
        return super.triggerEvent(state,level,pos,id,value);
    }
}
