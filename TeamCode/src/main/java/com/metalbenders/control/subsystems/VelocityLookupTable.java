package com.metalbenders.control.subsystems;

import java.util.Map;
import java.util.TreeMap;

public class VelocityLookupTable {

    // Map Horizontal Distance -> Motor Encoder Velocity
    private static final TreeMap<Double, Double> velocityTable = new TreeMap<>();

    static {
        velocityTable.put(24.0, 1015.0);
        velocityTable.put(28.0, 1025.0);
        velocityTable.put(32.0, 1050.0);
        velocityTable.put(36.0, 1070.0);
        velocityTable.put(40.0, 1085.0);
        velocityTable.put(44.0, 1100.0);
        velocityTable.put(48.0, 1130.0);
        velocityTable.put(52.0, 1160.0);
        velocityTable.put(56.0, 1190.0);
        velocityTable.put(60.0, 1230.0);
        velocityTable.put(64.0, 1270.0);
    }

    /**
     * Calculates launch velocity for a 72mm flywheel.
     *
     * @param targetDistanceInches Horizontal distance to target
     * @return Launch motor velocity in tick per second
     */
    public static double getTargetVelocity(double targetDistanceInches) {
        if (velocityTable.containsKey(targetDistanceInches)) {
            return velocityTable.get(targetDistanceInches);
        }
        Map.Entry<Double, Double> lower = velocityTable.floorEntry(targetDistanceInches);
        Map.Entry<Double, Double> upper = velocityTable.ceilingEntry(targetDistanceInches);
        if (lower == null) return upper.getValue();
        if (upper == null) return lower.getValue();

        double d1 = lower.getKey();
        double v1 = lower.getValue();
        double d2 = upper.getKey();
        double v2 = upper.getValue();

        return v1 + ((targetDistanceInches - d1) / (d2 - d1)) * (v2 - v1);

    }


}
