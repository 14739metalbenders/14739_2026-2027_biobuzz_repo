package com.metalbenders.constants;

import com.metalbenders.constants.Constants.FieldSide;

import static com.metalbenders.constants.Constants.AllianceColor.BLUE;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static com.metalbenders.constants.Constants.FieldSide.AUDIENCE_SIDE;
import static com.metalbenders.constants.Constants.FieldSide.SCORE_SIDE;

public enum AprilTag {
    RED_SCORE_SIDE_INSIDE(30, Position.INSIDE, RED, SCORE_SIDE),
    RED_SCORE_SIDE_INSIDE_CENTER(31, Position.INSIDE_CENTER, RED, SCORE_SIDE),
    RED_SCORE_SIDE_OUTSIDE_CENTER(32, Position.OUTSIDE_CENTER, RED, SCORE_SIDE),
    RED_SCORE_SIDE_OUTSIDE(33, Position.OUTSIDE, RED, SCORE_SIDE),
    RED_AUDIENCE_SIDE_INSIDE(37, Position.INSIDE, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_INSIDE_CENTER(36, Position.INSIDE_CENTER, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_OUTSIDE_CENTER(35, Position.OUTSIDE_CENTER, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_OUTSIDE(34, Position.OUTSIDE, RED, AUDIENCE_SIDE),
    BLUE_SCORE_SIDE_INSIDE(45, Position.INSIDE, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_INSIDE_CENTER(44, Position.INSIDE_CENTER, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_OUTSIDE_CENTER(43, Position.OUTSIDE_CENTER, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_OUTSIDE(42, Position.OUTSIDE, BLUE, SCORE_SIDE),
    BLUE_AUDIENCE_SIDE_INSIDE(38, Position.INSIDE, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_INSIDE_CENTER(39, Position.INSIDE_CENTER, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_OUTSIDE_CENTER(40, Position.OUTSIDE_CENTER, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_OUTSIDE(41, Position.OUTSIDE, BLUE, AUDIENCE_SIDE);

    private final Constants.AllianceColor allianceColor;
    private final int id;
    private final Position position;
    private final FieldSide fieldSide;

    AprilTag(int id, Position position, Constants.AllianceColor allianceColor, FieldSide fieldSide) {
        this.allianceColor = allianceColor;
        this.id = id;
        this.position = position;
        this.fieldSide = fieldSide;
    }

    public Constants.AllianceColor getAllianceColor() {
        return allianceColor;
    }

    public int getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public static AprilTag getAprilTagById(int id) {
        for (AprilTag aprilTag : AprilTag.values()) {
            if (aprilTag.getId() == id) {
                return aprilTag;
            }
        }
        return null;
    }

    public enum Position {
        INSIDE,
        INSIDE_CENTER,
        OUTSIDE_CENTER,
        OUTSIDE
    }
}
