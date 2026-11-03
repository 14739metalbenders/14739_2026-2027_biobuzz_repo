package com.metalbenders.constants;

import com.pedropathing.math.Pose;

public enum AutoFieldBoundary {
    RED_FIELD_BOUNDARY(Constants.AllianceColor.RED, 0, 72, 0, 144),
    BLUE_FIELD_BOUNDARY(Constants.AllianceColor.BLUE, 72, 144, 0, 144),
    AUDIENCE_SIDE_FIELD_BOUNDARY(Constants.AllianceColor.RED, 0, 144, 0, 72),
    SCORE_SIDE_FIELD_BOUNDARY(Constants.AllianceColor.BLUE, 0, 144, 72, 144);

    private final Constants.AllianceColor allianceColor;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    AutoFieldBoundary(Constants.AllianceColor allianceColor, double minX, double maxX, double minY, double maxY) {
        this.allianceColor = allianceColor;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static AutoFieldBoundary getByAllianceColor(Constants.AllianceColor allianceColor) {
        for (AutoFieldBoundary range : AutoFieldBoundary.values()) {
            if (range.allianceColor == allianceColor) {
                return range;
            }
        }
        return null;
    }

    public boolean isPoseWithinBoundary(Pose pose) {
        return pose.x() >= minX && pose.x() <= maxX && pose.y() >= minY && pose.y() <= maxY;
    }
}
