package com.metalbenders.constants;

import com.pedropathing.math.Pose;

import static com.metalbenders.constants.AutoFieldBoundary.AUDIENCE_SIDE_FIELD_BOUNDARY;

public enum FieldSide {
    SCORE_SIDE,
    AUDIENCE_SIDE;

    public static FieldSide getParkFieldSide(Constants.AllianceColor allianceColor) {
        return allianceColor == Constants.AllianceColor.RED ? SCORE_SIDE : AUDIENCE_SIDE;
    }

    public static FieldSide getGardenFieldSide(Constants.AllianceColor allianceColor) {
        return allianceColor == Constants.AllianceColor.RED ? AUDIENCE_SIDE : SCORE_SIDE;
    }

    public static FieldSide getOtherFlowerFieldSide(Constants.AllianceColor allianceColor) {
        return allianceColor == Constants.AllianceColor.RED ? SCORE_SIDE : AUDIENCE_SIDE;
    }

    public static FieldSide getParkSideFlowerFieldSide(Constants.AllianceColor allianceColor) {
        return allianceColor == Constants.AllianceColor.RED ? AUDIENCE_SIDE : SCORE_SIDE;
    }

    public static FieldSide getStartingFieldSide(Constants.AllianceColor allianceColor, boolean startingOnActiveSide) {
        if (allianceColor == Constants.AllianceColor.RED) {
            return startingOnActiveSide ? AUDIENCE_SIDE : SCORE_SIDE;
        }
        return startingOnActiveSide ? SCORE_SIDE : AUDIENCE_SIDE;
    }

    public static FieldSide getStartingHiveCellFieldSide(Constants.AllianceColor allianceColor) {
        if (allianceColor == Constants.AllianceColor.RED) {
            return AUDIENCE_SIDE;
        }
        return SCORE_SIDE;
    }

    public static FieldSide getCurrentFieldSide(Pose currentPose) {
        if (AUDIENCE_SIDE_FIELD_BOUNDARY.isPoseWithinBoundary(currentPose)) {
            return AUDIENCE_SIDE;
        }
        return SCORE_SIDE;
    }
}