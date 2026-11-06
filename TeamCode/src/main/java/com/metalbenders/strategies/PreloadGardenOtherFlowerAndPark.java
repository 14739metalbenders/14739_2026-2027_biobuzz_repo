package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.*;
import com.metalbenders.control.subsystems.*;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.*;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.FieldLocation.*;

public class PreloadGardenOtherFlowerAndPark extends AutoStrategy {

    private final boolean useInsideWaypoints;

    public PreloadGardenOtherFlowerAndPark(boolean useInsideWaypoints) {
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
        scheduleAuto(driveSubsystem, intakeSubsystem, launchSubsystem, flowerToolSubsystem, startingOnActiveSide);
    }

    private void scheduleAuto(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            FlowerToolSubsystem flowerToolSubsystem,
            boolean startingOnActiveSide) {
        if (startingOnActiveSide) {
            scheduleAutoActiveSide(driveSubsystem, intakeSubsystem, launchSubsystem, flowerToolSubsystem);
        } else {
            scheduleAutoInactiveSide(driveSubsystem, intakeSubsystem, launchSubsystem, flowerToolSubsystem);
        }
    }

    private void scheduleAutoActiveSide(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            FlowerToolSubsystem flowerToolSubsystem) {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(
                //preload
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),

                //garden
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor)).withTimeout(2000),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                                .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new WaitCommand(500),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(oppositeFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),

                //non-park side flower
                new SetFlowerToolState(flowerToolSubsystem, DOWN),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, FieldLocation.OTHER_FLOWER.getPose(allianceColor), FLOWER_COLLECTION_MAX_SPEED)
                            .withTimeout(MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new WaitCommand(Constants.MILLIS_TO_WAIT_FOR_FLOWER_INTAKE),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, getFlowerBackupPose(allianceColor)),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.STOP),

                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(oppositeFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new SetFlowerToolState(flowerToolSubsystem, UP),

                //park
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(oppositeFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }

    private void scheduleAutoInactiveSide(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            FlowerToolSubsystem flowerToolSubsystem) {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        commands.addCommands(
                //preload
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH),
                new AutoShoot(intakeSubsystem, launchSubsystem),

                //non-park side flower
                new SetFlowerToolState(flowerToolSubsystem, DOWN),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, FieldLocation.OTHER_FLOWER.getPose(allianceColor), FLOWER_COLLECTION_MAX_SPEED)
                                .withTimeout(MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new WaitCommand(Constants.MILLIS_TO_WAIT_FOR_FLOWER_INTAKE),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, getFlowerBackupPose(allianceColor)),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.STOP),

                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(startingFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new SetFlowerToolState(flowerToolSubsystem, UP),

                //garden
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor)).withTimeout(2000),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                                .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new ParallelDeadlineGroup(
                        new WaitCommand(500),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPosition(driveSubsystem, FieldLocation.getLaunchLocation(oppositeFieldSide).getPose(allianceColor)),
                new AutoShoot(intakeSubsystem, launchSubsystem),
                new DriveToPosition(driveSubsystem, getWaypointPose(oppositeFieldSide)),
                new DriveToPosition(driveSubsystem, getWaypointPose(startingFieldSide)),

                //park
                new DriveToPosition(driveSubsystem, FieldLocation.getOuterWaypointLocation(startingFieldSide).getPose(allianceColor)),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation(startingFieldSide).getPose(allianceColor)));
        CommandScheduler.getInstance().schedule(commands);
    }

    private Pose getWaypointPose(FieldSide fieldSide) {
        return useInsideWaypoints ? FieldLocation.getInnerWaypointLocation(fieldSide).getPose(allianceColor)
                : FieldLocation.getOuterWaypointLocation(fieldSide).getPose(allianceColor);
    }

    private Pose getFlowerBackupPose(AllianceColor allianceColor) {
        Pose flowerPose = OTHER_FLOWER.getPose(allianceColor);
        if (allianceColor == RED) {
            return new Pose(flowerPose.x(), flowerPose.y() - 10, flowerPose.heading());
        }
        return new Pose(flowerPose.x(), flowerPose.y() + 10, flowerPose.heading());
    }
}
