package com.metalbenders.constants;

import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;

public enum FieldLocation {
    AUDIENCE_SIDE_START_POSE(
            new Pose(57.75, 7, Math.toRadians(90)),
            new Pose(82.7, 7, Math.toRadians(90))),
    GARDEN_SIDE_FLOWER(
            new Pose(47.4, 129.9, Math.toRadians(90)),
            new Pose(94.4, 10.7, Math.toRadians(270))),
    PARK_SIDE_FLOWER(
            new Pose(9.6, 46.8, Math.toRadians(180)),
            new Pose(129.9, 93.2, Math.toRadians(0))),
    AUDIENCE_SIDE_LAUNCH(
            new Pose(57.75, 21.6, Math.toRadians(90)),
            new Pose(85.5, 21.6, Math.toRadians(90))),
    AUDIENCE_SIDE_WAYPOINT(
            new Pose(38, 37.7, Math.toRadians(90)),
            new Pose(105.8, 36.5, Math.toRadians(90))),
    AUDIENCE_SIDE_HIVE_CELL(
            new Pose(57.7, 57.7, Math.toRadians(270)),
            new Pose(83.8, 57.7, Math.toRadians(270))),
    SCORE_SIDE_START_POSE(
            new Pose(58.6, 134, Math.toRadians(270)),
            new Pose(82.4, 134, Math.toRadians(0))),
    SCORE_SIDE_LAUNCH(
            new Pose(58.3, 118.7, Math.toRadians(270)),
            new Pose(82.7, 118.4, Math.toRadians(270))),
    SCORE_SIDE_WAYPOINT(
            new Pose(36.3, 103.2, Math.toRadians(270)),
            new Pose(102.6, 102.3, Math.toRadians(270))),
    SCORE_SIDE_HIVE_CELL(
            new Pose(58.6, 82.3, Math.toRadians(90)),
            new Pose(84.4, 82.3, Math.toRadians(90))),
    GARDEN(
            new Pose(23.4, 6.6, Math.toRadians(180)),
            new Pose(119.3, 134.9, Math.toRadians(0))),
    PARK(
            new Pose(12.8, 92.9, Math.toRadians(90)),
            new Pose(128.2, 50.6, Math.toRadians(270)));
    private final Pose redPose;
    private final Pose bluePose;

    FieldLocation(Pose redPose, Pose bluePose) {
        this.redPose = redPose;
        this.bluePose = bluePose;
    }

    public Pose getPose(AllianceColor alliance) {
        return alliance == AllianceColor.RED ? redPose : bluePose;
    }
}