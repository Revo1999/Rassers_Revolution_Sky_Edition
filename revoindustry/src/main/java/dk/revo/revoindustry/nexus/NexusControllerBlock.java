package dk.revo.revoindustry.nexus;

import java.util.function.BiFunction;
import aztech.modern_industrialization.machines.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** MI's base block has no block-event forwarding; completion flashes need that client route. */
public final class NexusControllerBlock extends MachineBlock {
    public NexusControllerBlock(BiFunction<BlockPos,BlockState,? extends MachineBlockEntity> factory,Properties properties){super(factory,properties);}
    @Override public boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int value){
        if(level.getBlockEntity(pos) instanceof NexusMachine machine)return machine.triggerEvent(id,value);
        return super.triggerEvent(state,level,pos,id,value);
    }
}
