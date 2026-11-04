package dk.revo.revolib.energy;
/** Optional-MI storage boundary used by electric Revo machines, separate from no-storage converters. */
public interface EuStorage {
    long receive(long amount,boolean simulate);
    long getAmount();
    long getCapacity();
    default long extract(long amount,boolean simulate){return 0;}
    default boolean canReceive(){return true;}
    default boolean canExtract(){return false;}
}
