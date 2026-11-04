package dk.revo.revolib.energy;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DirectPowerConversionTest{
 final AtomicLong time=new AtomicLong(),fe=new AtomicLong(),eu=new AtomicLong();final AtomicInteger ratio=new AtomicInteger(10),rate=new AtomicInteger(32);
 long in(AtomicLong a,long n,boolean s){if(!s)a.addAndGet(n);return n;}
 long out(AtomicLong a,long n,boolean s){long x=Math.min(a.get(),n);if(!s)a.addAndGet(-x);return x;}
 DirectPowerConversion converter(){return new DirectPowerConversion(ratio::get,rate::get,time::get,()->{},(n,s)->out(fe,n,s),(n,s)->in(fe,n,s),(n,s)->out(eu,n,s),(n,s)->in(eu,n,s));}
 @Test void forwardOnlyWholeQuantaAndNoStoredRemainder(){var c=converter();assertEquals(0,c.receiveFe(9,false));assertEquals(0,eu.get());assertEquals(10,c.receiveFe(19,false));assertEquals(1,eu.get());c.setMode(false);assertEquals(0,c.extractFe(9,false));assertEquals(10,c.extractFe(10,false));assertEquals(0,eu.get());}
 @Test void simulationAndSpamShareOneBudget(){var c=converter();for(int i=0;i<100;i++)assertEquals(320,c.receiveFe(Integer.MAX_VALUE,true));assertEquals(0,eu.get());int sum=0;for(int i=0;i<100;i++)sum+=c.receiveFe(10,false);assertEquals(320,sum);c.setMode(false);assertEquals(0,c.extractFe(10,false));time.incrementAndGet();assertEquals(320,c.extractFe(Integer.MAX_VALUE,false));assertEquals(0,eu.get());}
 @Test void allTierLimitsInBothDirections(){for(int limit:new int[]{32,128,512}){rate.set(limit);time.incrementAndGet();var c=converter();assertEquals(limit*10,c.receiveFe(Integer.MAX_VALUE,false));assertEquals(0,c.receiveFe(10,false));time.incrementAndGet();c.setMode(false);assertEquals(limit*10,c.extractFe(Integer.MAX_VALUE,false));assertEquals(0,eu.get());}}
 @Test void reversePushAndForwardPullUseCorrectUnits(){var c=converter();c.setMode(false);assertEquals(32,c.receiveEu(Long.MAX_VALUE,false));assertEquals(320,fe.get());time.incrementAndGet();c.setMode(true);assertEquals(32,c.extractEu(Long.MAX_VALUE,false));assertEquals(0,fe.get());}
 @Test void blockedOutputAcceptsNothing(){var c=new DirectPowerConversion(()->10,()->32,time::get,()->{},(n,s)->0,(n,s)->0,(n,s)->0,(n,s)->0);assertEquals(0,c.receiveFe(10000,false));c.setMode(false);assertEquals(0,c.receiveEu(10000,false));}
 @Test void configurableRatioAndNegativeInputs(){ratio.set(16);var c=converter();assertEquals(512,c.receiveFe(Integer.MAX_VALUE,false));assertEquals(32,eu.get());assertEquals(0,c.receiveFe(-1,false));time.incrementAndGet();c.setMode(false);assertEquals(512,c.extractFe(Integer.MAX_VALUE,false));assertEquals(0,c.receiveEu(-1,false));}
 @Test void cyclicConvertersDoNotRecurseOrGeneratePower(){DirectPowerConversion[] c=new DirectPowerConversion[2];c[0]=new DirectPowerConversion(()->10,()->32,time::get,()->{},(n,s)->0,(n,s)->0,(n,s)->0,(n,s)->c[1].extractEu(n,s));c[1]=new DirectPowerConversion(()->10,()->32,time::get,()->{},(n,s)->c[0].receiveFe((int)n,s),(n,s)->0,(n,s)->0,(n,s)->0);assertEquals(0,c[0].receiveFe(320,false));}
}
