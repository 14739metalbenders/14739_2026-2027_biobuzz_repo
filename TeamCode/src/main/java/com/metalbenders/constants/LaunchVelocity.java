package com.metalbenders.constants;

public enum LaunchVelocity {
    IDLE(1060.0, 0.0, 24.0),
    VELOCITY_24_26(1015.0, 24.0, 26.0),
    VELOCITY_26_28(1020.0, 26.0, 28.0),
    VELOCITY_28_30(1025.0, 28.0, 30.0),
    VELOCITY_30_32(1035.0, 30.0, 32.0),
    VELOCITY_32_34(1050.0, 32.0, 34.0),
    VELOCITY_34_36(1060.0, 34.0, 36.0),
    VELOCITY_36_38(1070.0, 36.0, 38.0),
    VELOCITY_38_40(1080.0, 38.0, 40.0),
    VELOCITY_40_42(1085.0, 40.0, 42.0),
    VELOCITY_42_44(1090.0, 42.0, 44.0),
    VELOCITY_44_46(1100.0, 44.0, 46.0),
    VELOCITY_46_48(1125.0, 46.0, 48.0),
    VELOCITY_48_50(1130.0, 48.0, 50.0),
    VELOCITY_50_54(1160.0, 50.0, 54.0),
    MAX(1180.0, 54.0, 60.0),
    AUTONOMOUS(1050.0, null, null);


    private final Double minDistance;
    private final Double maxDistance;
    private final Double velocity;

    LaunchVelocity(Double velocity, Double minDistance, Double maxDistance) {
        this.velocity = velocity;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
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

    public Double getVelocity() {
        return velocity;
    }

    public Double getMinDistance() {
        return minDistance;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }
}
