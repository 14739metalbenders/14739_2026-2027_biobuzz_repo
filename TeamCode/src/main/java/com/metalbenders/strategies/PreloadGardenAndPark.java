package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.AutoShoot;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.commands.SetIntakeState;
import com.metalbenders.control.subsystems.*;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;
import static com.metalbenders.constants.FieldLocation.*;

public class PreloadGardenAndPark extends AutoStrategy {

    private final boolean useInsideWaypoints;

    public PreloadGardenAndPark(boolean useInsideWaypoints) {
        this.useInsideWaypoints = useInsideWaypoints;
    }
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
        if (startingOnActiveSide) {
            scheduleAutoActiveSide(driveSubsystem, intakeSubsystem, launchSubsystem);
        } else {
            scheduleAutoInactiveSide(driveSubsystem, intakeSubsystem, launchSubsystem);
        }
    }

    private void scheduleAutoActiveSide(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem) {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor)).withTimeout(2000),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                                .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(oppositeFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(oppositeFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }

    private void scheduleAutoInactiveSide(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem) {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor)).withTimeout(2000),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                                .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }

    private Pose getWaypointPose(FieldSide fieldSide) {
        return useInsideWaypoints ? FieldLocation.getInnerWaypointLocation(fieldSide).getPose(allianceColor)
                : FieldLocation.getOuterWaypointLocation(fieldSide).getPose(allianceColor);
    }
}
