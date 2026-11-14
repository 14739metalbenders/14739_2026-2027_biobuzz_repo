package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelRaceGroup;
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

import static com.metalbenders.constants.Constants.DEFAULT_TIME_TO_WAIT_FOR_TRANSITION_STATE_MILLIS;

public class ShootPreload extends SequentialCommandGroup {

    public ShootPreload(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            LimelightSubsystem limelightSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Supplier<FieldSide> activeHiveCellFieldSideSupplier,
            boolean waitToShoot) {
        //drive to launch location on current field side
        new DriveToPosition(
                driveSubsystem,
                () -> FieldLocation.getLaunchLocation(
                        FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose())).getPose(allianceColor));
        //wait for the hive to switch
        if (waitToShoot) {
            addCommands(new WaitForHiveSwitch(
                    limelightSubsystem, () -> FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose())));
        }
        //drive to the launch location and shoot
        addCommands(
                new DriveToLaunchAndShoot(
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        activeHiveCellFieldSideSupplier,
                        waitToShoot
                                ? Constants.MAX_TIME_TO_WAIT_FOR_TRANSITION_WHEN_STARTING_INACTIVE
                                : DEFAULT_TIME_TO_WAIT_FOR_TRANSITION_STATE_MILLIS));
    }
}
