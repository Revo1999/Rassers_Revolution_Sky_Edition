package dk.revo.revocultivation;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public class FarmFilter extends FarmPart {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SPECIES=IntegerProperty.create("species",0,7);
    private final int speciesCount;
    public FarmFilter(){this(false);}
    public FarmFilter(boolean steel){super(Properties.of().strength(3).noOcclusion().sound(SoundType.METAL));speciesCount=steel?8:5;registerDefaultState(stateDefinition.any().setValue(FORMED,false).setValue(FACING,Direction.NORTH).setValue(SPECIES,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FACING,SPECIES);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){
        if(!l.isClientSide){int species=(s.getValue(SPECIES)+1)%speciesCount;l.setBlockAndUpdate(pos,s.setValue(SPECIES,species));player.displayClientMessage(Component.translatable("message.revocultivation.filter",Component.translatable("program.revocultivation."+MachineKind.programName((speciesCount==8?MachineKind.STEEL_HARVESTER:MachineKind.HARVESTER).harvesterPrograms()[species]))),true);}
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
