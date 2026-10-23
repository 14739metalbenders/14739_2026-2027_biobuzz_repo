package com.metalbenders.constants;

public enum LaunchVelocity {
    IDLE(1180.0, 0.0, 26.0, 100.0),
    VELOCITY_26_30(0.0, 26.0, 30.0, 100.0),
    VELOCITY_30_34(0.0, 30.0, 34.0, 100.0),
    VELOCITY_34_38(0.0, 34.0, 38.0, 100.0),
    VELOCITY_38_42(0.0, 38.0, 42.0, 100.0),
    MAX(0.0, 26.0, 30.0, 100.0);


    private final Double velocity;
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
                if (distance >= launchVelocity.getMinDistance() && distance <= launchVelocity.getMaxDistance()) {
                    return launchVelocity;
                }
            }
        }
        return IDLE;
    }
}
