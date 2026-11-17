package dk.revo.revoextraction;

import java.util.List;

/** Split a scrolling tile at its one wrap boundary; never sample a neighboring atlas tile. */
public final class SluiceFlow {
    private SluiceFlow() {}
    public record Strip(float start, float end, float v0, float v1) {}
    public static List<Strip> strips(double scroll) {
        double shift = (scroll % 16 + 16) % 16;
        if (!Double.isFinite(shift) || shift < 0.00001 || 16-shift < 0.00001)
            return List.of(new Strip(0,16,0,1));
        float boundary = (float) shift;
        return List.of(new Strip(0,boundary,1-boundary/16,1), new Strip(boundary,16,0,1-boundary/16));
    }
}
