package com.metalbenders.opmodes.auto.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemMap;
import com.pedropathing.math.Pose;

public class AudienceSideTestStrategy extends AutoStrategy {

    @Override
    public void initialize(Constants.AllianceColor allianceColor, Pose parkPose, SubsystemMap subsystemMap) {
        // Initialize any necessary components or variables for the Audience Side Test Strategy
        CommandScheduler.getInstance().schedule(new SequentialCommandGroup(
        ));
    }

    @Override
    public Constants.FieldSide getStartingFieldSide() {
        return Constants.FieldSide.AUDIENCE_SIDE;
    }
}
