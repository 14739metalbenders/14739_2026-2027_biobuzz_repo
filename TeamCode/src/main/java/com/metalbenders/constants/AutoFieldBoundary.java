package com.metalbenders.constants;

import com.pedropathing.math.Pose;

public enum AutoFieldBoundary {
    RED_FIELD_BOUNDARY(0, 72, 0, 144),
    BLUE_FIELD_BOUNDARY(72, 144, 0, 144),
    AUDIENCE_SIDE_FIELD_BOUNDARY(0, 144, 0, 72),
    SCORE_SIDE_FIELD_BOUNDARY(0, 144, 72, 144),
    UNDER_HIVE_FIELD_BOUNDARY(48, 96, 48, 96);

    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    AutoFieldBoundary(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public boolean isPoseWithinBoundary(Pose pose) {
        return pose.x() >= minX && pose.x() <= maxX && pose.y() >= minY && pose.y() <= maxY;
    }
}
