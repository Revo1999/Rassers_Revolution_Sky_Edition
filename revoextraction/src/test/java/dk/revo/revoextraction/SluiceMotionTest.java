package dk.revo.revoextraction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SluiceMotionTest {
    @Test void verticalYokeStaysOnPinAndLeavesShaftAndWaterClearance(){
        for(int i=0;i<=1440;i++){
            double angle=i*Math.PI*2/1440;
            var m=SluiceMotion.at(angle);
            assertTrue(m.lift()>=-.5000001&&m.lift()<=.5000001);
            assertEquals(8+.5*Math.sin(angle),8+m.lift(),1e-9);
            assertEquals(7+m.lift(),5.6+1.4*m.springScale(),1e-9);
            assertEquals(3+m.lift(),1.6+1.4*m.springScale(),1e-9);
            assertTrue(11.5+m.lift()>8+Math.sqrt(8),"Floor clears rotating 4x4 shaft");
            assertTrue(12+.35+m.lift()<13,"Riffles stay submerged under steady water");
        }
        assertEquals(0,SluiceMotion.at(0).lift());
        assertEquals(.5,SluiceMotion.at(Math.PI/2).lift());
    }
}
