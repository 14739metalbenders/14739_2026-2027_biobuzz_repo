package com.metalbenders.util;

import com.metalbenders.constants.FieldSide;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

public class AutonomousStorage {
    public static Pose endPose = DEFAULT_POSE;
    public static FieldSide currentActiveHiveSide = FieldSide.SCORING;

}
