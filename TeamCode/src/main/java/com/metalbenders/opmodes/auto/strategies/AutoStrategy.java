package com.metalbenders.opmodes.auto.strategies;

import com.metalbenders.constants.Constants;
import com.pedropathing.math.Pose;

public abstract class AutoStrategy {

    public abstract void initialize(Constants.AllianceColor allianceColor, Pose parkPose);
    public abstract Constants.FieldSide getStartingFieldSide();
}
