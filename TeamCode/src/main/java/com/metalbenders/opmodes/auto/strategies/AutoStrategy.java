package com.metalbenders.opmodes.auto.strategies;

import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemMap;
import com.pedropathing.math.Pose;

public abstract class AutoStrategy {

    public abstract void initialize(Constants.AllianceColor allianceColor, Pose parkPose, SubsystemMap subsystemMap);
    public abstract Constants.FieldSide getStartingFieldSide();
}
