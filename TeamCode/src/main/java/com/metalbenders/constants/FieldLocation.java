package com.metalbenders.constants;

import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;

public enum FieldLocation {
    //AUDIENCE_SIDE specific locations
    AUDIENCE_SIDE_START_POSE(
            new Pose(57.75, 7, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_LAUNCH(
            new Pose(57.75, 20, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_OUTER_WAYPOINT(
            new Pose(30, 34, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_INNER_WAYPOINT(
            new Pose(60, 34, Math.toRadians(90)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_HIVE_CELL(
            new Pose(57.75, 57, Math.toRadians(270)), Symmetry.MIRROR_X),
    AUDIENCE_SIDE_PARK(
            new Pose(9.8, 95.5, Math.toRadians(90)), Symmetry.ROTATE_180),
    //SCORE_SIDE specific locations
    SCORE_SIDE_START_POSE(
            new Pose(57.75, 137, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_LAUNCH(
            new Pose(57.75, 124, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_OUTER_WAYPOINT(
            new Pose(30, 110, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_INNER_WAYPOINT(
            new Pose(60, 110, Math.toRadians(270)), Symmetry.MIRROR_X),
    SCORE_SIDE_HIVE_CELL(
            new Pose(57.75, 87, Math.toRadians(90)), Symmetry.MIRROR_X),
    SCORE_SIDE_PARK(
            new Pose(14.5, 91, Math.toRadians(90)), Symmetry.ROTATE_180),
    //Flower Locations
    OTHER_FLOWER(
            new Pose(48, 136, Math.toRadians(90)), Symmetry.ROTATE_180),
    PARK_SIDE_FLOWER(
            new Pose(8, 48, Math.toRadians(180)), Symmetry.ROTATE_180),
    //Garden Locations
    GARDEN_START(
            new Pose(20, 7, Math.toRadians(180)), Symmetry.ROTATE_180),
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

    public static FieldLocation getLaunchLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_LAUNCH, AUDIENCE_SIDE_LAUNCH);
    }

    public static FieldLocation getOuterWaypointLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_OUTER_WAYPOINT, AUDIENCE_SIDE_OUTER_WAYPOINT);
    }

    public static FieldLocation getInnerWaypointLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_INNER_WAYPOINT, AUDIENCE_SIDE_INNER_WAYPOINT);
    }

    public static FieldLocation getParkLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_PARK, AUDIENCE_SIDE_PARK);
    }

    public static FieldLocation getStartLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_START_POSE, AUDIENCE_SIDE_START_POSE);
    }

    public static FieldLocation getHiveCellLocation(Constants.FieldSide fieldSide) {
        return selectByFieldSide(fieldSide, SCORE_SIDE_HIVE_CELL, AUDIENCE_SIDE_HIVE_CELL);
    }

    private static FieldLocation selectByFieldSide(
            Constants.FieldSide fieldSide, FieldLocation scoreSide, FieldLocation audienceSide) {
        return fieldSide == Constants.FieldSide.SCORE_SIDE ? scoreSide : audienceSide;
    }
}