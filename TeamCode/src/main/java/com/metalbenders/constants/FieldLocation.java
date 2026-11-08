package com.metalbenders.constants;

import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;

public enum FieldLocation {
    //AUDIENCE_SIDE specific locations
    AUDIENCE_SIDE_START_POSE(
            new Pose(57.75, 7, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_LAUNCH(
            new Pose(57.75, 30, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_OUTER_WAYPOINT(
            new Pose(30, 34, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_INNER_WAYPOINT(
            new Pose(60, 34, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_HIVE_CELL(
            new Pose(57.75, 57, Math.toRadians(270)), Symmetry.MIRROR_X),
    PARK(
            new Pose(10, 92, Math.toRadians(0)), Symmetry.ROTATE_180),
    //SCORE_SIDE specific locations
    SCORE_SIDE_START_POSE(
            new Pose(57.75, 137, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_LAUNCH(
            new Pose(57.75, 114, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_OUTER_WAYPOINT(
            new Pose(30, 110, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_INNER_WAYPOINT(
            new Pose(60, 110, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_HIVE_CELL(
            new Pose(57.75, 87, Math.toRadians(90)), Symmetry.MIRROR_X),
    //Flower Locations
    OTHER_FLOWER(
            new Pose(47, 131.5, Math.toRadians(90)), Symmetry.ROTATE_180),
    PARK_SIDE_FLOWER(
            new Pose(14, 48, Math.toRadians(180)), Symmetry.ROTATE_180),
    //Garden Locations
    GARDEN_START(
            new Pose(22, 7, Math.toRadians(180)), Symmetry.ROTATE_180),
    GARDEN_END(
            new Pose(7.0, 7, Math.toRadians(180)), Symmetry.ROTATE_180);

    private enum Symmetry {
        MIRROR_X,
        ROTATE_180;

        private Pose apply(Pose pose) {
            double heading = this == MIRROR_X ? Math.PI - pose.heading() : pose.heading() + Math.PI;
            double fullTurn = 2 * Math.PI;
            heading = ((heading % fullTurn) + fullTurn) % fullTurn;
            return new Pose(144 - pose.x(), this == MIRROR_X ? pose.y() : 144 - pose.y(), heading);
        }
    }

    private final Pose redPose;
    private final Pose bluePose;

    FieldLocation(Pose redPose, Symmetry symmetry) {
        this.redPose = redPose;
        this.bluePose = symmetry.apply(redPose);
    }

    public Pose getPose(AllianceColor alliance) {
        return alliance == AllianceColor.RED ? redPose : bluePose;
    }

    public static FieldLocation getLaunchLocation(FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_LAUNCH, AUDIENCE_SIDE_LAUNCH);
    }

    public static FieldLocation getOuterWaypointLocation(FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_OUTER_WAYPOINT, AUDIENCE_SIDE_OUTER_WAYPOINT);
    }

    public static FieldLocation getInnerWaypointLocation(FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_INNER_WAYPOINT, AUDIENCE_SIDE_INNER_WAYPOINT);
    }

    public static FieldLocation getParkLocation(FieldSide fieldSide) {
        return PARK;
    }

    public static FieldLocation getStartLocation(FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_START_POSE, AUDIENCE_SIDE_START_POSE);
    }

    public static FieldLocation getHiveCellLocation(FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_HIVE_CELL, AUDIENCE_SIDE_HIVE_CELL);
    }

    private static FieldLocation selectByFieldSide(
            FieldSide fieldSide, FieldLocation scoreSide, FieldLocation audienceSide) {
        return fieldSide == FieldSide.SCORE_SIDE ? scoreSide : audienceSide;
    }
}