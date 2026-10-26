package com.metalbenders.autostrategies;

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
        boolean scoreSide = startingFieldSide == Constants.FieldSide.SCORE_SIDE;
        FieldLocation parkLocation = scoreSide ? SCORE_SIDE_PARK : AUDIENCE_SIDE_PARK;
        CommandScheduler.getInstance().schedule(
                new DriveToPositionCommand(driveSubsystem, parkLocation.getPose(allianceColor)));
    }
}
