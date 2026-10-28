package com.metalbenders.control.commands;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_DURING_AUTO_SHOOT;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;

public class AutoShoot extends SequentialCommandGroup {

    public AutoShoot(IntakeSubsystem intakeSubsystem, LaunchSubsystem launchSubsystem) {
        super(
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.OPEN),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.SHOOT)
                        .withTimeout(MILLIS_TO_WAIT_DURING_AUTO_SHOOT),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.STOP),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.KICK),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.CLOSED));
    }
}
