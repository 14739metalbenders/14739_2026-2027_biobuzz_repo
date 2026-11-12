package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

import java.util.function.Supplier;

public class DriveToLaunchAndShoot extends SequentialCommandGroup {

    public DriveToLaunchAndShoot(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            LimelightSubsystem limelightSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Supplier<FieldSide> activeHiveCellFieldSideSupplier) {
        this(
                driveSubsystem,
                intakeSubsystem,
                launchSubsystem,
                turretSubsystem,
                limelightSubsystem,
                allianceColor,
                waypointSeriesType,
                activeHiveCellFieldSideSupplier,
                Constants.DEFAULT_TIME_TO_WAIT_FOR_TRANSITION_STATE_MILLIS);
    }

    public DriveToLaunchAndShoot(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            LimelightSubsystem limelightSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Supplier<FieldSide> activeHiveCellFieldSideSupplier,
            long timeToWaitForHiveSwitchMillis) {
        addCommands(
                //drive to launch location on current field side
                new DriveToPosition(
                        driveSubsystem,
                        () -> FieldLocation
                                .getLaunchLocation(FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose()))
                                .getPose(allianceColor)),
                //check if the hive is transitioning
                new CheckForHiveTransition(limelightSubsystem, timeToWaitForHiveSwitchMillis),
                //drive to the launch location of the current hive state field side
                new DriveToTargetFieldSide(
                        driveSubsystem,
                        () -> limelightSubsystem.getHiveState().getFieldSide(),
                        allianceColor,
                        waypointSeriesType),
                //wait for the hive to switch to the active field side if it is not already on that side
                new ConditionalCommand(
                        new InstantCommand(),
                        new WaitForHiveSwitch(limelightSubsystem, () -> limelightSubsystem.getHiveState().getFieldSide()),
                        () -> limelightSubsystem.getHiveState().isActive()
                ),
                //drive to the launch location of the active hive cell field side
                new DriveToPosition(
                        driveSubsystem,
                        () -> FieldLocation
                                .getLaunchLocation(activeHiveCellFieldSideSupplier.get())
                                .getPose(allianceColor)),
                //shoot
                new AutoShoot(intakeSubsystem, launchSubsystem, turretSubsystem));
    }
}