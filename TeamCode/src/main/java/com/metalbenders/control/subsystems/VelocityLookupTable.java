package com.metalbenders.control.subsystems;

import java.util.Map;
import java.util.TreeMap;

public class VelocityLookupTable {

    // Map Horizontal Distance -> Motor Encoder Velocity
    private static final TreeMap<Double, Double> velocityTable = new TreeMap<>();

    static {
        velocityTable.put(8.0, 1030.0);
        velocityTable.put(11.0, 1030.0);
        velocityTable.put(16.0, 1045.0);
        velocityTable.put(18.0, 1045.0);
        velocityTable.put(20.0, 1095.0);
        velocityTable.put(22.0, 1107.0);
        velocityTable.put(24.0, 1113.0);
        velocityTable.put(26.0, 1115.0);
        velocityTable.put(28.0, 1135.0);
        velocityTable.put(30.0, 1140.0);
        velocityTable.put(32.0, 1155.0);
        velocityTable.put(34.0, 1160.0);
        velocityTable.put(36.0, 1170.0);
        velocityTable.put(38.0, 1180.0);
        velocityTable.put(40.0, 1200.0);
        velocityTable.put(47.0, 1240.0);
        velocityTable.put(55.0, 1280.0);
        velocityTable.put(60.0, 1330.0);
        velocityTable.put(65.0, 1380.0);
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
