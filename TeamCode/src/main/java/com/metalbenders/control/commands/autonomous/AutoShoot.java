package com.metalbenders.control.commands.autonomous;

import static com.metalbenders.constants.Constants.AUTO_SHOOT_TIMEOUT_MILLIS;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.SetIntakeState;
import com.metalbenders.control.commands.SetLaunchGateState;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;

import java.util.function.Supplier;

public class AutoShoot extends SequentialCommandGroup {

    public AutoShoot(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            Supplier<FieldSide> currentFieldSide,
            Supplier<FieldSide> activeFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        this(
                driveSubsystem,
                intakeSubsystem,
                launchSubsystem,
                null,
                currentFieldSide,
                activeFieldSide,
                allianceColor,
                useInsideWaypoints);
    }

    public AutoShoot(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            Long timeToWaitBeforeShooting,
            Supplier<FieldSide> currentFieldSide,
            Supplier<FieldSide> activeFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        addCommands(
                new DriveToLaunch(
                        driveSubsystem,
                        currentFieldSide,
                        activeFieldSide,
                        allianceColor,
                        useInsideWaypoints));
        if (timeToWaitBeforeShooting != null) {
            addCommands(new WaitCommand(timeToWaitBeforeShooting));
        }
        addCommands(
                new SetLaunchGateState(launchSubsystem, Constants.LaunchGateState.OPEN),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.SHOOT)
                        .withTimeout(AUTO_SHOOT_TIMEOUT_MILLIS),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.STOP),
                new SetLaunchGateState(launchSubsystem, Constants.LaunchGateState.KICK),
                new SetLaunchGateState(launchSubsystem, Constants.LaunchGateState.CLOSED));
    }
}
