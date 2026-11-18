package dk.revo.revoextraction;

import java.util.function.IntUnaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/** One small, server-authored snapshot shared by menus and Jade. Coordinates are controller offsets. */
public record DrillDiagnostics(int progress,int rpm,int state,int part,int x,int y,int z,int direction) {
    public static final int SIZE=8;
    public static DrillDiagnostics capture(DrillMachine machine){
        var issue=machine.facingIssue();var offset=issue==null?net.minecraft.core.BlockPos.ZERO:issue.position().subtract(machine.getBlockPos());
        return new DrillDiagnostics(machine.progress(),Math.min(32767,Math.round(Math.abs(machine.inputSpeed()))),machine.operationalState(),
            issue==null?0:issue.part().ordinal()+1,offset.getX(),offset.getY(),offset.getZ(),issue==null?0:issue.expected().get3DDataValue());
    }
    public static DrillDiagnostics read(IntUnaryOperator get){return new DrillDiagnostics(get.applyAsInt(0),get.applyAsInt(1),get.applyAsInt(2),get.applyAsInt(3),get.applyAsInt(4),get.applyAsInt(5),get.applyAsInt(6),get.applyAsInt(7));}
    public int get(int i){return switch(i){case 0->progress;case 1->rpm;case 2->state;case 3->part;case 4->x;case 5->y;case 6->z;case 7->direction;default->0;};}
    public int[] values(){return new int[]{progress,rpm,state,part,x,y,z,direction};}
    private String stateKey(){return switch(state){case 1->"working";case 2->"no_drive";case 3->"output_blocked";case 4->"unloaded";case 5->"wrong_facing";case 6->"too_slow";case 7->"dry";case 8->"broken";case 9->"sediment_blocked";default->"incomplete";};}
    public Component stateText(){return Component.translatable("gui.revoextraction.state."+stateKey());}
    public Component hint(){
        if(state!=5)return Component.translatable("gui.revoextraction.hint."+stateKey());
        var type=part>0&&part<=DrillLayout.Part.values().length?DrillLayout.Part.values()[part-1]:DrillLayout.Part.CONTROLLER;
        String name=switch(type){case INPUT->"input";case HATCH->"hatch";case FLUID_HATCH->"fluid_hatch";default->"controller";};
        return Component.translatable("gui.revoextraction.hint.wrong_facing",Component.translatable("gui.revoextraction.part."+name),x,y,z,
            Component.translatable("gui.revoextraction.direction."+Direction.from3DDataValue(direction).getName()));
    }
}
