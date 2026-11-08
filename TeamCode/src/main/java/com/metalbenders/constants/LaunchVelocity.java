package com.metalbenders.constants;

public enum LaunchVelocity {
    IDLE(1185.0, 0.0, 24.0, 100.0),
    VELOCITY_24_26(1130.0, 24.0, 26.0, 100.0),
    VELOCITY_26_28(1140.0, 26.0, 28.0, 100.0),
    VELOCITY_28_30(1165.0, 28.0, 30.0, 100.0),
    VELOCITY_30_32(1165.0, 30.0, 32.0, 100.0),
    VELOCITY_32_34(1170.0, 32.0, 34.0, 100.0),
    VELOCITY_34_36(1185.0, 34.0, 36.0, 100.0),
    VELOCITY_36_38(1185.0, 36.0, 38.0, 100.0),
    VELOCITY_38_40(1210.0, 38.0, 40.0, 100.0),
    VELOCITY_40_42(1235.0, 40.0, 42.0, 100.0),
    VELOCITY_42_44(1245.0, 42.0, 44.0, 100.0),
    VELOCITY_44_46(1250.0, 44.0, 46.0, 100.0),
    VELOCITY_46_48(1265.0, 46.0, 48.0, 100.0),
    VELOCITY_48_50(1280.0, 48.0, 50.0, 100.0),
    VELOCITY_50_54(1290.0, 50.0, 54.0, 100.0),
    MAX(1300.0, 54.0, 60.0, 100.0),
    AUTONOMOUS(1150.0, null, null, 100.0);


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
