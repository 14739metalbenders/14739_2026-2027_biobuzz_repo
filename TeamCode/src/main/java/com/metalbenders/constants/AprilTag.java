package com.metalbenders.constants;

import com.metalbenders.constants.Constants.FieldSide;

import static com.metalbenders.constants.AprilTag.Position.*;
import static com.metalbenders.constants.Constants.AllianceColor.BLUE;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static com.metalbenders.constants.Constants.FieldSide.AUDIENCE_SIDE;
import static com.metalbenders.constants.Constants.FieldSide.SCORE_SIDE;

public enum AprilTag {
    RED_SCORE_SIDE_INSIDE(30, RED, SCORE_SIDE, INSIDE),
    RED_SCORE_SIDE_INSIDE_CENTER(31, RED, SCORE_SIDE, INSIDE_CENTER),
    RED_SCORE_SIDE_OUTSIDE_CENTER(32, RED, SCORE_SIDE, OUTSIDE_CENTER),
    RED_SCORE_SIDE_OUTSIDE(33, RED, SCORE_SIDE, OUTSIDE),
    RED_AUDIENCE_SIDE_INSIDE(37, RED, AUDIENCE_SIDE, INSIDE),
    RED_AUDIENCE_SIDE_INSIDE_CENTER(36, RED, AUDIENCE_SIDE, INSIDE_CENTER),
    RED_AUDIENCE_SIDE_OUTSIDE_CENTER(35, RED, AUDIENCE_SIDE, OUTSIDE_CENTER),
    RED_AUDIENCE_SIDE_OUTSIDE(34, RED, AUDIENCE_SIDE, OUTSIDE),
    BLUE_SCORE_SIDE_INSIDE(45, BLUE, SCORE_SIDE, INSIDE),
    BLUE_SCORE_SIDE_INSIDE_CENTER(44, BLUE, SCORE_SIDE, INSIDE_CENTER),
    BLUE_SCORE_SIDE_OUTSIDE_CENTER(43, BLUE, SCORE_SIDE, OUTSIDE_CENTER),
    BLUE_SCORE_SIDE_OUTSIDE(42, BLUE, SCORE_SIDE, OUTSIDE),
    BLUE_AUDIENCE_SIDE_INSIDE(38, BLUE, AUDIENCE_SIDE, INSIDE),
    BLUE_AUDIENCE_SIDE_INSIDE_CENTER(39, BLUE, AUDIENCE_SIDE, INSIDE_CENTER),
    BLUE_AUDIENCE_SIDE_OUTSIDE_CENTER(40, BLUE, AUDIENCE_SIDE, OUTSIDE_CENTER),
    BLUE_AUDIENCE_SIDE_OUTSIDE(41, BLUE, AUDIENCE_SIDE, OUTSIDE);

    private final Constants.AllianceColor allianceColor;
    private final int id;
    private final Position position;
    private final FieldSide fieldSide;

    AprilTag(int id, Constants.AllianceColor allianceColor, FieldSide fieldSide, Position position) {
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

    public FieldSide getFieldSide() {
        return fieldSide;
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
