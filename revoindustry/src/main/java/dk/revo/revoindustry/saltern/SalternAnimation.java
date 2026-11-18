package dk.revo.revoindustry.saltern;

/** Paid recipe time drives every effect, so stalled machines hold their pose. */
public final class SalternAnimation {
    private SalternAnimation() {}
    public static float smooth(float value) { float t=Math.clamp(value,0,1);return t*t*(3-2*t); }
    public static float growth(float tick,int bud) { return .025f+.975f*smooth((tick-bud*18)/(300-bud*18)); }
    public static float harvest(float tick,int bud) { return smooth((tick-330-bud*8)/28); }
    public static float height(float tick,int bud) { return growth(tick,bud)*(1-.98f*harvest(tick,bud)); }
    public static float width(float tick,int bud) { return (.65f+.25f*growth(tick,bud))*(1-.65f*harvest(tick,bud))*(bud==3?.55f:1); }
}
