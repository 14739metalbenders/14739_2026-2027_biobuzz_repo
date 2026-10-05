package com.metalbenders.constants;

import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;

public enum FieldLocations {
    AUDIENCE_SIDE_START_POSE(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    AUDIENCE_SIDE_FLOWER(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    AUDIENCE_SIDE_LAUNCH(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    AUDIENCE_SIDE_WAYPOINT(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    SCORE_SIDE_START_POSE(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    SCORE_SIDE_FLOWER(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    SCORE_SIDE_LAUNCH(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    SCORE_SIDE_WAYPOINT(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    GARDEN(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0))),
    PARK(
            new Pose(0, 0, Math.toRadians(0)),
            new Pose(0, 0, Math.toRadians(0)));
    private final Pose redPose;
    private final Pose bluePose;

    FieldLocations(Pose redPose, Pose bluePose) {
        this.redPose = redPose;
        this.bluePose = bluePose;
    }

    public Pose getPose(AllianceColor alliance) {
        return alliance == AllianceColor.RED ? redPose : bluePose;
    }
}