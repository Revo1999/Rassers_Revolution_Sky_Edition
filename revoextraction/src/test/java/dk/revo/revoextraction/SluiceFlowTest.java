package dk.revo.revoextraction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SluiceFlowTest {
    @Test void movingAtlasStripsCoverChannelAtWrapAndLargeWorldTimes() {
        double[] phases={0,1e-12,0.1,1,8,15.999999999,16,16.000000001,-1,1e12+7.5};
        for(double phase:phases) {
            var strips=SluiceFlow.strips(phase);
            assertTrue(strips.size()<=2);
            float end=0;
            for(var strip:strips) {
                assertEquals(end,strip.start(),1e-6);
                assertTrue(strip.end()>strip.start());
                assertTrue(strip.v0()>=0&&strip.v1()<=1&&strip.v1()>strip.v0());
                assertEquals((strip.end()-strip.start())/16,strip.v1()-strip.v0(),1e-6);
                end=strip.end();
            }
            assertEquals(16,end);
        }
    }
    @Test void waterPatternTravelsDownstreamAndLoops() {
        assertEquals(2,SluiceFlow.strips(2).get(0).end());
        assertEquals(3,SluiceFlow.strips(3).get(0).end());
        assertEquals(SluiceFlow.strips(2),SluiceFlow.strips(18));
    }
}
