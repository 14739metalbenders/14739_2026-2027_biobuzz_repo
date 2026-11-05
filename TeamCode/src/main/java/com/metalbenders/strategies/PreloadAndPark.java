package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.AutoShoot;
import com.metalbenders.control.commands.DriveToPosition;
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
            FlowerToolSubsystem flowerToolSubsystem,
            boolean startingOnActiveSide) {
        scheduleAuto(driveSubsystem, intakeSubsystem, launchSubsystem, startingOnActiveSide);
    }

    private void scheduleAuto(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            boolean startingOnActiveSide) {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)));
        if (!startingOnActiveSide) {
            commands.addCommands(new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH));
        }
        commands.addCommands(
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }
}
