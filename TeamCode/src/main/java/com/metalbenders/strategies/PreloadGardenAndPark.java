package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.control.commands.AutoShoot;
import com.metalbenders.control.commands.DriveToPositionCommand;
import com.metalbenders.control.commands.SetIntakeStateCommand;
import com.metalbenders.control.subsystems.*;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;
import static com.metalbenders.constants.FieldLocation.*;

public class PreloadGardenAndPark extends AutoStrategy {

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
                new DriveToPositionCommand(driveSubsystem, GARDEN_START.getPose(allianceColor)),
                new ParallelDeadlineGroup(
                        new DriveToPositionCommand(driveSubsystem, GARDEN_END.getPose(allianceColor)),
                        new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getLaunchLocation(oppositeFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getWaypointLocation(oppositeFieldSide).getPose(allianceColor)),
                new DriveToPositionCommand(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }
}
