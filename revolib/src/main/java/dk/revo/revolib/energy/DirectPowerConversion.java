package dk.revo.revolib.energy;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/** Forwards accepted energy immediately. It contains no energy balance or capacity. */
public final class DirectPowerConversion {
 @FunctionalInterface public interface Transfer { long move(long amount,boolean simulate); }
 private final IntSupplier ratio,rate;private final LongSupplier clock;private final Runnable changed;
 private final Transfer feSource,feSink,euSource,euSink;
 private long tick=Long.MIN_VALUE;private int movedEu;private boolean feToEu=true,busy;
 public DirectPowerConversion(IntSupplier ratio,IntSupplier rate,LongSupplier clock,Runnable changed,Transfer feSource,Transfer feSink,Transfer euSource,Transfer euSink){this.ratio=ratio;this.rate=rate;this.clock=clock;this.changed=changed;this.feSource=feSource;this.feSink=feSink;this.euSource=euSource;this.euSink=euSink;}
 public int ratio(){return Math.clamp(ratio.getAsInt(),1,1000);}public int rateEu(){return Math.max(1,rate.getAsInt());}
 public boolean feToEu(){return feToEu;}public void setMode(boolean value){if(feToEu!=value){feToEu=value;changed.run();}}
 private int remaining(){long now=clock.getAsLong();if(tick!=now){tick=now;movedEu=0;}return Math.max(0,rateEu()-movedEu);}
 private long move(long requestedEu,boolean simulate,Transfer target,boolean targetFe,boolean chargeInput){
  if(busy||requestedEu<=0)return 0;long offered=Math.min(requestedEu,remaining());if(offered==0)return 0;
  busy=true;try{
   long quantum=targetFe?ratio():1;long available=Math.clamp(target.move(offered*quantum,true),0,offered*quantum)/quantum;
   if(available==0)return 0;if(simulate)return available;
   long actual=Math.clamp(target.move(available*quantum,false),0,available*quantum);
   int spent=(int)((actual+quantum-1)/quantum);movedEu+=spent;
   // Charge complete input quanta, but never report output not actually extracted.
   return chargeInput?spent:actual/quantum;
  }finally{busy=false;}
 }
 public int receiveFe(int n,boolean sim){return feToEu?(int)(move(Math.max(0,n)/ratio(),sim,euSink,false,true)*ratio()):0;}
 public int extractFe(int n,boolean sim){return feToEu?0:(int)(move(Math.max(0,n)/ratio(),sim,euSource,false,false)*ratio());}
 public long receiveEu(long n,boolean sim){return feToEu?0:move(n,sim,feSink,true,true);}
 public long extractEu(long n,boolean sim){return feToEu?move(n,sim,feSource,true,false):0;}
}
