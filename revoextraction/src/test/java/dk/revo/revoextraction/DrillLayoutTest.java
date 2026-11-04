package dk.revo.revoextraction;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DrillLayoutTest {
    @Test void materialsMatchSurvivalAssemblyInstructions() {
        var counts=new EnumMap<DrillLayout.Part,Integer>(DrillLayout.Part.class);
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++)counts.merge(DrillLayout.part(x,y,z),1,Integer::sum);
        assertEquals(Map.of(DrillLayout.Part.LOG,12,DrillLayout.Part.CASING,10,DrillLayout.Part.CORE,1,DrillLayout.Part.AIR,1,DrillLayout.Part.CONTROLLER,1,DrillLayout.Part.HATCH,1,DrillLayout.Part.INPUT,1),counts);
    }
    @Test void hatchAndDriveAreOppositeAndBitHasClearance() {
        assertEquals(DrillLayout.Part.CONTROLLER,DrillLayout.part(0,1,1));
        assertEquals(DrillLayout.Part.HATCH,DrillLayout.part(-1,0,0));
        assertEquals(DrillLayout.Part.CORE,DrillLayout.part(0,1,0));
        assertEquals(DrillLayout.Part.AIR,DrillLayout.part(0,0,0));
        assertEquals(DrillLayout.Part.INPUT,DrillLayout.part(1,1,0));
    }
    @Test void rejectsCoordinatesOutsideFootprint() {
        assertThrows(IllegalArgumentException.class,()->DrillLayout.part(2,1,0));
        assertThrows(IllegalArgumentException.class,()->DrillLayout.part(0,-1,0));
        assertThrows(IllegalArgumentException.class,()->DrillLayout.part(0,3,0));
    }
}
