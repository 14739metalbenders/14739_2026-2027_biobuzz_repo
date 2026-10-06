package com.metalbenders.constants;

import com.pedropathing.math.Pose;

public class Constants {

    public enum AllianceColor {
        RED,
        BLUE
    }

    public static final double AUTONOMOUS_DURATION_SECONDS = 30l;

    public static final Pose DEFAULT_POSE = new Pose(0,0,0);
}
