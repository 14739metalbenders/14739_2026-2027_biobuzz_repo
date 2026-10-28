package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.DriveToPositionCommand;
import com.metalbenders.control.subsystems.*;

import static com.metalbenders.constants.FieldLocation.*;

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
            boolean startingOnActiveSide) {
        CommandScheduler.getInstance().schedule(
                new DriveToPositionCommand(
                        driveSubsystem,
                        FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
    }
}
