package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.AutoShoot;
import com.metalbenders.control.commands.DriveToPositionCommand;
import com.metalbenders.control.subsystems.*;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;

public class PreloadAndPark extends AutoStrategy {

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
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(new DriveToPositionCommand(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)));
        if (waitBeforeStarting) {
            commands.addCommands(new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH));
        }
        commands.addCommands(
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getOuterWaypointLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }
}
