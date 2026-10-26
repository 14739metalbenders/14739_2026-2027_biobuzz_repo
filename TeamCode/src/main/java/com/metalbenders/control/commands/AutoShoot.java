package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.opmodes.auto.strategies.AutoStrategy;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_DURING_AUTO_SHOOT;

public class AutoShoot extends SequentialCommandGroup {

    private IntakeSubsystem intakeSubsystem;
    private LaunchSubsystem launchSubsystem;

    public AutoShoot(IntakeSubsystem intakeSubsystem, LaunchSubsystem launchSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        this.launchSubsystem = launchSubsystem;
        addCommands(
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.OPEN),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.INTAKE),
                new WaitCommand(MILLIS_TO_WAIT_DURING_AUTO_SHOOT),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.STOP),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.KICK),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.CLOSED));
        addRequirements(intakeSubsystem, launchSubsystem);
    }
}
