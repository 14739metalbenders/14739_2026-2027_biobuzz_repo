package com.metalbenders.control.commands.shoot;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;

import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.LaunchGateState.KICK;

public class StopShooting extends SequentialCommandGroup {

    public StopShooting(IntakeSubsystem intakeSubsystem, LaunchSubsystem launchSubsystem) {
        addCommands(
                new StopIntake(intakeSubsystem),
                new SetLaunchGateState(launchSubsystem, KICK),
                new SetLaunchGateState(launchSubsystem, CLOSED)
        );
        addRequirements(launchSubsystem);
    }
}
