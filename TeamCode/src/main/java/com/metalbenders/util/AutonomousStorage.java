package com.metalbenders.util;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

import com.metalbenders.constants.FieldSide;
import com.pedropathing.math.Pose;

public class AutonomousStorage {
    public static Pose endPose = DEFAULT_POSE;
    public static FieldSide currentActiveHiveSide = FieldSide.SCORE_SIDE;
}
