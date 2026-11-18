package dk.revo.revoextraction;
/** Coordinates: x is right, y is up, z is front. */
public final class DrillLayout {
    public enum Part { LOG, CASING, CORE, AIR, CONTROLLER, HATCH, INPUT, TANK, FLUID_HATCH }
    private DrillLayout() {}
    public static Part part(int x,int y,int z) {
        if (Math.abs(x)>1 || y<0 || y>2 || Math.abs(z)>1) throw new IllegalArgumentException("Outside 3x3x3 drill");
        if (Math.abs(x)==1 && Math.abs(z)==1) return Part.LOG;
        if (x==0 && z==0 && y==0) return Part.AIR;
        if (x==0 && z==0 && y==1) return Part.CORE;
        if (x==0 && z==1 && y==1) return Part.CONTROLLER;
        if (x==-1 && z==0 && y==0) return Part.HATCH;
        if (x==1 && z==0 && y==1) return Part.INPUT;
        return Part.CASING;
    }
}
