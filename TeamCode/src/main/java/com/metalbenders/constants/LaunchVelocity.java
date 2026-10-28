package com.metalbenders.constants;

import com.acmerobotics.dashboard.config.Config;

public enum LaunchVelocity {
    IDLE(1100.0, 0.0, 26.0, 100.0),
    VELOCITY_26_30(1140.0, 26.0, 30.0, 100.0),
    VELOCITY_30_34(1160.0, 30.0, 34.0, 100.0),
    VELOCITY_34_38(1180.0, 34.0, 38.0, 100.0),
    VELOCITY_38_42(1230.0, 38.0, 42.0, 100.0),
    VELOCITY_42_46(1250.0, 42.0, 46.0, 100.0),
    VELOCITY_46_50(1270.0, 46.0, 50.0, 100.0),
    VELOCITY_50_54(1310.0, 50.0, 54.0, 100.0),
    MAX(1350.0, 54.0, 60.0, 100.0);


    private Double velocity;
    private final Double minDistance;
    private final Double maxDistance;
    private final Double tolerance;

    LaunchVelocity(Double velocity, Double minDistance, Double maxDistance, Double tolerance) {
        this.velocity = velocity;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.tolerance = tolerance;
    }

    public Double getVelocity() {
        return velocity;
    }

    public Double getMinDistance() {
        return minDistance;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public static LaunchVelocity getLaunchVelocityByDistance(double distance) {
        if (distance > MAX.minDistance) {
            return MAX;
        }
        for (LaunchVelocity launchVelocity : LaunchVelocity.values()) {
            if (launchVelocity.getMinDistance() != null && launchVelocity.getMaxDistance() != null) {
                if (distance >= launchVelocity.getMinDistance() && distance < launchVelocity.getMaxDistance()) {
                    return launchVelocity;
                }
            }
        }
        return IDLE;
    }
}
