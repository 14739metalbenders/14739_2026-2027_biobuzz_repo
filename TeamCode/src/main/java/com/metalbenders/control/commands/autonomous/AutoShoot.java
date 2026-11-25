package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.subsystems.SubsystemManager;

import java.util.function.BooleanSupplier;

import static com.metalbenders.constants.AutoFieldBoundary.inLaunchArea;
import static com.metalbenders.constants.Constants.IntakeState.SHOOT;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.LaunchGateState.KICK;
import static com.metalbenders.constants.Constants.LaunchGateState.OPEN;
import static com.metalbenders.constants.Constants.TIME_TO_WAIT_TO_SHOOT_BEFORE_KICK;

public class AutoShoot extends SequentialCommandGroup {

    private final SubsystemManager subsystemManager;

    public AutoShoot(SubsystemManager subsystemManager, AutoAbort abort) {
        this(subsystemManager, abort.requestCommand(), () -> true);
    }

    public AutoShoot(SubsystemManager subsystemManager, Command onFailure, BooleanSupplier canShoot) {
        this(subsystemManager, onFailure, canShoot, true);
    }

    public AutoShoot(SubsystemManager subsystemManager, Command onFailure,
                     BooleanSupplier canShoot, boolean delayBeforeShot) {
        this.subsystemManager = subsystemManager;
        if (delayBeforeShot) {
            addCommands(new WaitCommand(500));
        }
        addCommands(
                new ConditionalCommand(
                        shootAndKick(),
                        onFailure,
                        () -> inLaunchArea(subsystemManager.getDriveSubsystem().getCurrentPose())
                                && canShoot.getAsBoolean())
        );

        //Note: turretSubsystem is not used in this command, but it is added as a requirement so the turret doesn't
        // move while shooting
        addRequirements(
                subsystemManager.getIntakeSubsystem(),
                subsystemManager.getLaunchSubsystem(),
                subsystemManager.getTurretSubsystem());
    }

    private SequentialCommandGroup shootAndKick() {
        return new SequentialCommandGroup(
                new ParallelDeadlineGroup(
                        new WaitCommand(TIME_TO_WAIT_TO_SHOOT_BEFORE_KICK),
                        new SetLaunchGateState(subsystemManager, OPEN),
                        new RunIntake(subsystemManager, SHOOT)
                ),
                new StopIntake(subsystemManager),
                new SetLaunchGateState(subsystemManager, KICK),
                new SetLaunchGateState(subsystemManager, CLOSED));
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        if (interrupted) {
            //Sub-commands were canceled mid-sequence, so put the hardware in a safe state directly
            subsystemManager.getIntakeSubsystem().setIntakeState(Constants.IntakeState.STOP);
            subsystemManager.getLaunchSubsystem().setLaunchGateState(CLOSED);
        }
    }
}
