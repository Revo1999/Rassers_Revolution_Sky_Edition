package dk.revo.revoindustry.saltern;

import java.util.*;
import dk.revo.revoindustry.hatchery.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public final class SalternLayout {
    public record Cell(BlockPos offset,Block block,Direction face,String role){}
    public static List<Cell> cells(){
        var cells=new ArrayList<Cell>();
        for(int y=0;y<3;y++)for(int z=0;z<3;z++)for(int x=-1;x<=1;x++){
            Block block=Hatchery.CASING.get();Direction face=Direction.NORTH;String role="casing";
            if(x==0&&y==1&&z==1){block=Blocks.AIR;role="air";}
            else if(y==1&&(x==0||z==1)){
                block=Hatchery.GLASS.get();role="glass";face=z==0?Direction.NORTH:z==2?Direction.SOUTH:x==-1?Direction.WEST:Direction.EAST;
            }
            if(y==0){
                if(x==0&&z==0){block=Saltern.CONTROLLER.get();role="controller";}
                if(x==-1&&z==1){block=Saltern.WATER.get();face=Direction.WEST;role="water";}
                if(x==1&&z==1){block=Saltern.BRINE.get();face=Direction.EAST;role="brine";}
                if(x==0&&z==2){block=Saltern.ENERGY.get();face=Direction.SOUTH;role="energy";}
                if(x==0&&z==1){block=Saltern.CORE.get();role="core";}
            }
            cells.add(new Cell(new BlockPos(x,y,z),block,face,role));
        }
        return List.copyOf(cells);
    }
    public static BlockPos at(BlockPos origin,Direction front,int x,int y,int z){return HatcheryLayout.at(origin,front,x,y,z);}
    public static BlockPos at(BlockPos origin,Direction front,BlockPos offset){return HatcheryLayout.at(origin,front,offset);}
    public static BlockState state(Cell c,Direction front){var state=c.block.defaultBlockState();return state.hasProperty(HatcheryBlock.FACING)?state.setValue(HatcheryBlock.FACING,HatcheryLayout.rotate(c.face,front)):state;}
}
