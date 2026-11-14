package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.flowertool.SetFlowerToolState;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;
import com.pedropathing.math.Pose;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.AllianceColor;
import static com.metalbenders.constants.Constants.AllianceColor.isRed;
import static com.metalbenders.constants.Constants.AutoChallengeType;
import static com.metalbenders.constants.Constants.FLOWER_COLLECTION_MAX_SPEED;
import static com.metalbenders.constants.Constants.FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION;
import static com.metalbenders.constants.Constants.FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING;
import static com.metalbenders.constants.Constants.FLOWER_DISTANCE_TO_BACKUP_WHEN_FINISHED_COLLECTING;
import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_FLOWER_INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER;
import static com.metalbenders.constants.FieldLocation.HIVE_SIDE_FLOWER;
import static com.metalbenders.constants.FieldLocation.PARK_SIDE_FLOWER;

public class CollectAndShootFlower extends SequentialCommandGroup {

    public CollectAndShootFlower(
            FlowerToolSubsystem flowerToolSubsystem,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            LimelightSubsystem limelightSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Constants.AutoChallengeType autoChallengeType,
            Supplier<FieldSide> activeHiveCellFieldSideSupplier) {
        super (
                new ParallelCommandGroup(
                        new DriveToTargetFieldSide(
                                driveSubsystem,
                                getFlowerFieldSide(autoChallengeType, allianceColor),
                                allianceColor,
                                waypointSeriesType),
                        new SetFlowerToolState(flowerToolSubsystem, DOWN)),
                new RunIntake(intakeSubsystem, INTAKE),
                collectFromFLower(driveSubsystem, autoChallengeType, allianceColor),
                new StopIntake(intakeSubsystem),
                new DriveToLaunchAndShoot(
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        activeHiveCellFieldSideSupplier),
                new SetFlowerToolState(flowerToolSubsystem, UP));
    }

    private static SequentialCommandGroup collectFromFLower(
            DriveSubsystem driveSubsystem,
            AutoChallengeType autoChallengeType,
            AllianceColor allianceColor) {
        Pose flowerPose = getFlowerPose(autoChallengeType, allianceColor);
        return new SequentialCommandGroup(
                new DriveToPosition(driveSubsystem, flowerPose, FLOWER_COLLECTION_MAX_SPEED)
                        .withTimeout(MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL),
                new WaitCommand(MILLIS_TO_WAIT_FOR_FLOWER_INTAKE),
                oscillateOnFlower(flowerPose, driveSubsystem),
                new DriveToPosition(
                        driveSubsystem,
                        getFlowerBackupPose(autoChallengeType, allianceColor, FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING)));
//                new DriveToPosition(driveSubsystem, flowerPose, FLOWER_COLLECTION_MAX_SPEED)
//                        .withTimeout(MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL),
//                new WaitCommand(MILLIS_TO_WAIT_FOR_FLOWER_INTAKE),
//                oscillateOnFlower(flowerPose, driveSubsystem),
//                new DriveToPosition(
//                        driveSubsystem,
//                        getFlowerBackupPose(autoChallengeType, allianceColor, FLOWER_DISTANCE_TO_BACKUP_WHEN_FINISHED_COLLECTING)));
    }

    private static Pose getFlowerPose(AutoChallengeType autoChallengeType, AllianceColor allianceColor) {
        if (autoChallengeType == AutoChallengeType.PARK_SIDE_FLOWER) {
            return PARK_SIDE_FLOWER.getPose(allianceColor);
        } else {
            return HIVE_SIDE_FLOWER.getPose(allianceColor);
        }
    }

    private static FieldSide getFlowerFieldSide(AutoChallengeType autoChallengeType, AllianceColor allianceColor) {
        if (autoChallengeType == AutoChallengeType.PARK_SIDE_FLOWER) {
            return FieldSide.getParkSideFlowerFieldSide(allianceColor);
        } else {
            return FieldSide.getHiveSideFlowerFieldSide(allianceColor);
        }
    }

    private static SequentialCommandGroup oscillateOnFlower(Pose flowerPose, DriveSubsystem driveSubsystem) {
        return new SequentialCommandGroup(
                new DriveToPosition(
                        driveSubsystem,
                        flowerPose.withHeading(flowerPose.heading() + FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                        .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                new DriveToPosition(
                        driveSubsystem,
                        flowerPose.withHeading(flowerPose.heading() - FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                        .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                new DriveToPosition(
                        driveSubsystem,
                        flowerPose.withHeading(flowerPose.heading() + FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                        .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                new DriveToPosition(
                        driveSubsystem,
                        flowerPose.withHeading(flowerPose.heading() - FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                        .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER));
    }

    private static Pose getFlowerBackupPose(
            AutoChallengeType autoChallengeType, Constants.AllianceColor allianceColor, double distanceToBackup) {
        Pose flowerPose = getFlowerPose(autoChallengeType, allianceColor);
        if (autoChallengeType == AutoChallengeType.PARK_SIDE_FLOWER) {
            if (isRed(allianceColor)) {
                return flowerPose.withX(flowerPose.x() + distanceToBackup);
            }
            return flowerPose.withX(flowerPose.x() - distanceToBackup);
        } else {
            if (isRed(allianceColor)) {
                return flowerPose.withY(flowerPose.y() - distanceToBackup);
            }
            return flowerPose.withY(flowerPose.y() + distanceToBackup);
        }
    }
}
