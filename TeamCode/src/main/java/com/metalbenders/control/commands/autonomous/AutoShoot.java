package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

import static com.metalbenders.constants.Constants.IntakeState.SHOOT;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.LaunchGateState.KICK;
import static com.metalbenders.constants.Constants.LaunchGateState.OPEN;
import static com.metalbenders.constants.Constants.TIME_TO_WAIT_TO_SHOOT_BEFORE_KICK;

public class AutoShoot extends SequentialCommandGroup {

    private final IntakeSubsystem intakeSubsystem;
    private final LaunchSubsystem launchSubsystem;

    public AutoShoot(IntakeSubsystem intakeSubsystem, LaunchSubsystem launchSubsystem, TurretSubsystem turretSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        this.launchSubsystem = launchSubsystem;
        addCommands(
                new ParallelDeadlineGroup(
                        new WaitCommand(TIME_TO_WAIT_TO_SHOOT_BEFORE_KICK),
                        new SetLaunchGateState(launchSubsystem, OPEN),
                        new RunIntake(intakeSubsystem, SHOOT)
                ),
                new StopIntake(intakeSubsystem),
                new SetLaunchGateState(launchSubsystem, KICK),
                new SetLaunchGateState(launchSubsystem, CLOSED)
        );
        //Note: turretSubsystem is not used in this command, but it is added as a requirement so the turret doesn't
        // move while shooting
        addRequirements(intakeSubsystem, launchSubsystem, turretSubsystem);
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        if (interrupted) {
            //Sub-commands were canceled mid-sequence, so put the hardware in a safe state directly
            intakeSubsystem.setIntakeState(Constants.IntakeState.STOP);
            launchSubsystem.setLaunchGateState(CLOSED);
        }
    }
}
