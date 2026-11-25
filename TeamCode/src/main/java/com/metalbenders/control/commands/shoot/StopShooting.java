package com.metalbenders.control.commands.shoot;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.LaunchGateState.KICK;

public class StopShooting extends SequentialCommandGroup {

    public StopShooting(SubsystemManager subsystemManager) {
        addCommands(
                new StopIntake(subsystemManager),
                new SetLaunchGateState(subsystemManager, KICK),
                new SetLaunchGateState(subsystemManager, CLOSED)
        );
        addRequirements(subsystemManager.getLaunchSubsystem());
    }
}
