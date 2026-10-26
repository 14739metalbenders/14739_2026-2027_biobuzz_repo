package com.metalbenders.autostrategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.AutoShoot;
import com.metalbenders.control.commands.DriveToPositionCommand;
import com.metalbenders.control.subsystems.*;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;
import static com.metalbenders.constants.FieldLocation.*;

public class ShootPreloadAndPark extends AutoStrategy {

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
        scheduleAuto(driveSubsystem, intakeSubsystem, launchSubsystem, !startingOnActiveSide);
    }

    private void scheduleAuto(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            boolean waitBeforeStarting) {
        boolean scoreSide = startingFieldSide == Constants.FieldSide.SCORE_SIDE;
        FieldLocation launchLocation = scoreSide ? SCORE_SIDE_LAUNCH : AUDIENCE_SIDE_LAUNCH;
        FieldLocation parkLocation = scoreSide ? SCORE_SIDE_PARK : AUDIENCE_SIDE_PARK;

        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(new DriveToPositionCommand(driveSubsystem, launchLocation.getPose(allianceColor)));
        if (waitBeforeStarting) {
            commands.addCommands(new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH));
        }
        commands.addCommands(
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPositionCommand(driveSubsystem, parkLocation.getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }
}
