
package com.metalbenders.constants;

import com.pedropathing.math.Pose;

import static com.metalbenders.constants.AutoFieldBoundary.AUDIENCE_SIDE_FIELD_BOUNDARY;
import static com.metalbenders.constants.Constants.AllianceColor.isRed;

public enum FieldSide {
    SCORING,
    AUDIENCE;

    public static FieldSide getParkFieldSide(Constants.AllianceColor allianceColor) {
        return isRed(allianceColor) ? SCORING : AUDIENCE;
    }

    public static FieldSide getGardenFieldSide(Constants.AllianceColor allianceColor) {
        return isRed(allianceColor) ? AUDIENCE : SCORING;
    }

    public static FieldSide getHiveSideFlowerFieldSide(Constants.AllianceColor allianceColor) {
        return getParkFieldSide(allianceColor);
    }

    public static FieldSide getParkSideFlowerFieldSide(Constants.AllianceColor allianceColor) {
        return getGardenFieldSide(allianceColor);
    }

    public static FieldSide getRobotStartingFieldSide(
            Constants.AllianceColor allianceColor, boolean startingOnActiveSide) {
        return startingOnActiveSide
                ? getGardenFieldSide(allianceColor)
                : getParkFieldSide(allianceColor);
    }

    public static FieldSide getStartingHiveCellFieldSide(Constants.AllianceColor allianceColor) {
        return getGardenFieldSide(allianceColor);
    }

    public static FieldSide getCurrentFieldSide(Pose currentPose) {
        return AUDIENCE_SIDE_FIELD_BOUNDARY.isPoseWithinBoundary(currentPose)
                ? AUDIENCE
                : SCORING;
    }

    public FieldSide getOppositeSide() {
        return this == SCORING ? AUDIENCE : SCORING;
    }
}