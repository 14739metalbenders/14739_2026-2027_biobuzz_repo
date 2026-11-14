package com.metalbenders.control.subsystems;

import java.util.Map;
import java.util.TreeMap;

public class VelocityLookupTable {

    // Map Horizontal Distance -> Motor Encoder Velocity
    private static final TreeMap<Double, Double> velocityTable = new TreeMap<>();

    static {
        velocityTable.put(21.1, 990.0);
        velocityTable.put(27.3, 1000.0);
        velocityTable.put(33.4, 1030.0);
        velocityTable.put(38.0, 1055.0);
        velocityTable.put(45.5, 1115.0);
        velocityTable.put(49.0, 1125.0);
        velocityTable.put(57.0, 1190.0);
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
