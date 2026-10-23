package com.metalbenders.opmodes.auto.strategies;

import com.metalbenders.constants.Constants;
import com.pedropathing.math.Pose;

public class AudienceSideTestStrategy extends AutoStrategy {

    @Override
    public void initialize(Constants.AllianceColor allianceColor, Pose parkPose) {
        // Initialize any necessary components or variables for the Audience Side Test Strategy
    }

    @Override
    public Constants.FieldSide getStartingFieldSide() {
        return Constants.FieldSide.AUDIENCE_SIDE;
    }
}
