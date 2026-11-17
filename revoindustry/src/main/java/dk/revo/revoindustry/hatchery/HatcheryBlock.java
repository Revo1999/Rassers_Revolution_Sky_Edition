package dk.revo.revoindustry.hatchery;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class HatcheryBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE=BlockStateProperties.LIT;
    public final Hatchery.Part part;
    public HatcheryBlock(Hatchery.Part part){super(Properties.of().strength(8,1200).sound(part==Hatchery.Part.GLASS?SoundType.GLASS:SoundType.METAL).noOcclusion().lightLevel(s->(part==Hatchery.Part.LIGHT||part==Hatchery.Part.CASING)&&s.getValue(ACTIVE)?12:0));this.part=part;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ACTIVE,false));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ACTIVE);}
    public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return switch(part){case CONTROLLER->new HatcheryController(p,s);case INPUT,OUTPUT,ENERGY->new HatcheryPort(p,s);default->null;};}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){
        if(l.isClientSide)return null;
        if(t==Hatchery.CONTROLLER_ENTITY.get())return (world,pos,state,be)->((HatcheryController)be).tick();
        if(t==Hatchery.PORT_ENTITY.get())return (world,pos,state,be)->((HatcheryPort)be).tick();
        return null;
    }
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
        var be=l.getBlockEntity(pos);
        if(be instanceof HatcheryController c){if(!l.isClientSide)p.openMenu(c,b->b.writeBlockPos(pos).writeBoolean(false));return InteractionResult.sidedSuccess(l.isClientSide);}
        if(be instanceof HatcheryPort port&&part!=Hatchery.Part.ENERGY){if(!l.isClientSide)p.openMenu(port,b->b.writeBlockPos(pos).writeBoolean(true));return InteractionResult.sidedSuccess(l.isClientSide);}
        if(be instanceof HatcheryPort port&&part==Hatchery.Part.ENERGY){var c=port.controller();if(c!=null&&!l.isClientSide)p.openMenu(c,b->b.writeBlockPos(c.getBlockPos()).writeBoolean(false));return InteractionResult.sidedSuccess(l.isClientSide);}
        return InteractionResult.PASS;
    }
    protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
        if(!s.is(next.getBlock())&&!l.isClientSide){
            if(l.getBlockEntity(p) instanceof HatcheryController c)c.breakMachine();
            if(l.getBlockEntity(p) instanceof HatcheryPort port)port.dropContents();
        }
        super.onRemove(s,l,p,next,moving);
    }
    public boolean skipRendering(BlockState state,BlockState neighbor,Direction side){return part==Hatchery.Part.GLASS&&neighbor.is(this)||super.skipRendering(state,neighbor,side);}
}
