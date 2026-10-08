package com.metalbenders.constants;

import com.pedropathing.math.Pose;

public class Constants {

    public enum AllianceColor {
        RED,
        BLUE
    }

    public enum FieldSide {
        SCORE_SIDE,
        AUDIENCE_SIDE
    }

    public enum HivePosition {
        SCORE_SIDE_UP_AUDIENCE_SIDE_DOWN,
        AUDIENCE_SIDE_UP_SCORE_SIDE_DOWN
    }

    public static final double AUTONOMOUS_DURATION_SECONDS = 30l;

    public static final Pose DEFAULT_POSE = new Pose(0, 0, Math.toRadians(180));
}
