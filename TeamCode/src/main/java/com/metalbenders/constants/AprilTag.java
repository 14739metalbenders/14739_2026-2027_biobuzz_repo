package com.metalbenders.constants;

import com.metalbenders.constants.Constants.FieldSide;

import static com.metalbenders.constants.Constants.AllianceColor.BLUE;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static com.metalbenders.constants.Constants.FieldSide.AUDIENCE_SIDE;
import static com.metalbenders.constants.Constants.FieldSide.SCORE_SIDE;

public enum AprilTag {
    RED_SCORE_SIDE_INSIDE(30, RED, SCORE_SIDE),
    RED_SCORE_SIDE_INSIDE_CENTER(31, RED, SCORE_SIDE),
    RED_SCORE_SIDE_OUTSIDE_CENTER(32, RED, SCORE_SIDE),
    RED_SCORE_SIDE_OUTSIDE(33, RED, SCORE_SIDE),
    RED_AUDIENCE_SIDE_INSIDE(37, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_INSIDE_CENTER(36, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_OUTSIDE_CENTER(35, RED, AUDIENCE_SIDE),
    RED_AUDIENCE_SIDE_OUTSIDE(34, RED, AUDIENCE_SIDE),
    BLUE_SCORE_SIDE_INSIDE(45, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_INSIDE_CENTER(44, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_OUTSIDE_CENTER(43, BLUE, SCORE_SIDE),
    BLUE_SCORE_SIDE_OUTSIDE(42, BLUE, SCORE_SIDE),
    BLUE_AUDIENCE_SIDE_INSIDE(38, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_INSIDE_CENTER(39, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_OUTSIDE_CENTER(40, BLUE, AUDIENCE_SIDE),
    BLUE_AUDIENCE_SIDE_OUTSIDE(41, BLUE, AUDIENCE_SIDE);

    private final Constants.AllianceColor allianceColor;
    private final int id;
    private final FieldSide fieldSide;

    AprilTag(int id, Constants.AllianceColor allianceColor, FieldSide fieldSide) {
        this.allianceColor = allianceColor;
        this.id = id;
        this.fieldSide = fieldSide;
    }

    public Constants.AllianceColor getAllianceColor() {
        return allianceColor;
    }

    public int getId() {
        return id;
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
}
