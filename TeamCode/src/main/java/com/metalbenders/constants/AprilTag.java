package com.metalbenders.constants;

import static com.metalbenders.constants.Constants.AllianceColor.BLUE;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static com.metalbenders.constants.FieldSide.AUDIENCE;
import static com.metalbenders.constants.FieldSide.SCORING;

public enum AprilTag {
    RED_SCORE_SIDE_INSIDE(30, RED, SCORING),
    RED_SCORE_SIDE_INSIDE_CENTER(31, RED, SCORING),
    RED_SCORE_SIDE_OUTSIDE_CENTER(32, RED, SCORING),
    RED_SCORE_SIDE_OUTSIDE(33, RED, SCORING),
    RED_AUDIENCE_SIDE_INSIDE(37, RED, AUDIENCE),
    RED_AUDIENCE_SIDE_INSIDE_CENTER(36, RED, AUDIENCE),
    RED_AUDIENCE_SIDE_OUTSIDE_CENTER(35, RED, AUDIENCE),
    RED_AUDIENCE_SIDE_OUTSIDE(34, RED, AUDIENCE),
    BLUE_SCORE_SIDE_INSIDE(45, BLUE, SCORING),
    BLUE_SCORE_SIDE_INSIDE_CENTER(44, BLUE, SCORING),
    BLUE_SCORE_SIDE_OUTSIDE_CENTER(43, BLUE, SCORING),
    BLUE_SCORE_SIDE_OUTSIDE(42, BLUE, SCORING),
    BLUE_AUDIENCE_SIDE_INSIDE(38, BLUE, AUDIENCE),
    BLUE_AUDIENCE_SIDE_INSIDE_CENTER(39, BLUE, AUDIENCE),
    BLUE_AUDIENCE_SIDE_OUTSIDE_CENTER(40, BLUE, AUDIENCE),
    BLUE_AUDIENCE_SIDE_OUTSIDE(41, BLUE, AUDIENCE);

    private final Constants.AllianceColor allianceColor;
    private final int id;
    private final FieldSide fieldSide;

    AprilTag(int id, Constants.AllianceColor allianceColor, FieldSide fieldSide) {
        this.allianceColor = allianceColor;
        this.id = id;
        this.fieldSide = fieldSide;
    }

    public static AprilTag fromId(int id) {
        for (AprilTag aprilTag : AprilTag.values()) {
            if (aprilTag.getId() == id) {
                return aprilTag;
            }
        }
        return null;
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
}
