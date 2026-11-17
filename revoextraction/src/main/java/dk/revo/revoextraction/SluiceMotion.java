package dk.revo.revoextraction;

/** An eccentric pin slides in a horizontal yoke slot, producing vertical travel only. */
public final class SluiceMotion {
    private SluiceMotion() {}
    public record Pose(double lift, double springScale) {}
    public static Pose at(double radians) {
        double lift=.5*Math.sin(radians);
        return new Pose(lift,1+lift/1.4);
    }
}
