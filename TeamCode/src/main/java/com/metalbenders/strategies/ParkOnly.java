package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.subsystems.*;

public class ParkOnly extends AutoStrategy {

    @Override
    protected void initializeAuto(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem,
            FlowerToolSubsystem flowerToolSubsystem,
            boolean startingOnActiveSide) {
        CommandScheduler.getInstance().schedule(
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
    }
}
