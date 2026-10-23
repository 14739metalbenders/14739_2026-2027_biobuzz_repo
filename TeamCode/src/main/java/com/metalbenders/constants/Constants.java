package com.metalbenders.constants;

import com.pedropathing.math.Pose;

public class Constants {

    public enum OpModeType {
        AUTO,
        TELEOP
    }

    public enum AllianceColor {
        RED,
        BLUE
    }

    public enum FieldSide {
        SCORE_SIDE,
        AUDIENCE_SIDE
    }

    public enum ArtifactType {
        POLLEN,
        NECTAR
    }

    public static final double NECTAR_DIAMETER = 3.6; //inches
    public static final double POLLEN_DIAMETER = 2.8; //inches

    public static final Pose DEFAULT_POSE = new Pose(0, 0, Math.toRadians(90));
}
