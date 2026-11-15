package dk.revo.revoindustry.hatchery;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Canonical front is north. One blueprint drives validation, recipes, tests and EMI. */
public final class HatcheryLayout {
    public record Cell(BlockPos offset,Hatchery.Part part,Direction facing) {}
    public static final List<Cell> SHELL;
    static {
        var cells=new ArrayList<Cell>();
        for(int y=0;y<5;y++)for(int z=0;z<5;z++)for(int x=-2;x<=2;x++){
            if(x>-2&&x<2&&y>0&&y<4&&z>0&&z<4)continue;
            var part=Hatchery.Part.CASING;var face=Direction.NORTH;
            if(z==0&&Math.abs(x)<=1&&y>=1&&y<=3)part=Hatchery.Part.GLASS;
            if(z==0&&Math.abs(x)==2&&(y==1||y==3))part=Hatchery.Part.LIGHT;
            if(x==0&&y==0&&z==0)part=Hatchery.Part.CONTROLLER;
            if(x==-2&&y==0&&z==2){part=Hatchery.Part.INPUT;face=Direction.WEST;}
            if(x==2&&y==0&&z==2){part=Hatchery.Part.OUTPUT;face=Direction.EAST;}
            if(x==0&&y==0&&z==4){part=Hatchery.Part.ENERGY;face=Direction.SOUTH;}
            cells.add(new Cell(new BlockPos(x,y,z),part,face));
        }
        SHELL=List.copyOf(cells);
    }
    public static Block block(Hatchery.Part part){return switch(part){case CASING->Hatchery.CASING.get();case GLASS->Hatchery.GLASS.get();case LIGHT->Hatchery.LIGHT.get();case CONTROLLER->Hatchery.CONTROLLER.get();case INPUT->Hatchery.INPUT.get();case OUTPUT->Hatchery.OUTPUT.get();case ENERGY->Hatchery.ENERGY.get();};}
    public static BlockPos at(BlockPos origin,Direction front,int x,int y,int z){return origin.relative(front.getClockWise(),x).above(y).relative(front.getOpposite(),z);}
    public static BlockPos at(BlockPos origin,Direction front,BlockPos p){return at(origin,front,p.getX(),p.getY(),p.getZ());}
    public static Direction rotate(Direction canonical,Direction front){return switch(canonical){case NORTH->front;case SOUTH->front.getOpposite();case EAST->front.getClockWise();case WEST->front.getCounterClockWise();default->canonical;};}
    public static BlockState state(Cell c,Direction front){return block(c.part).defaultBlockState().setValue(HatcheryBlock.FACING,rotate(c.facing,front));}
    public static boolean directional(Hatchery.Part part){return part==Hatchery.Part.CONTROLLER||part==Hatchery.Part.INPUT||part==Hatchery.Part.OUTPUT||part==Hatchery.Part.ENERGY||part==Hatchery.Part.LIGHT||part==Hatchery.Part.GLASS;}
}
