package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.RaceSafeSequentialCommandGroup;
import com.metalbenders.control.commands.flowertool.SetFlowerToolState;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.SubsystemManager;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.AllianceColor;
import static com.metalbenders.constants.Constants.AllianceColor.isRed;
import static com.metalbenders.constants.Constants.AutoChallengeType;
import static com.metalbenders.constants.Constants.FLOWER_COLLECTION_MAX_SPEED;
import static com.metalbenders.constants.Constants.FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION;
import static com.metalbenders.constants.Constants.FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING;
import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_FLOWER_INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER;
import static com.metalbenders.constants.FieldLocation.HIVE_SIDE_FLOWER;
import static com.metalbenders.constants.FieldLocation.PARK_SIDE_FLOWER;

public class CollectAndShootFlower extends SequentialCommandGroup {

    private final SubsystemManager subsystemManager;

    public CollectAndShootFlower(
            SubsystemManager subsystemManager,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Constants.AutoChallengeType autoChallengeType,
            boolean allowFieldCrossing,
            AutoAbort abort) {
        this.subsystemManager = subsystemManager;
        addCommands (
                new ParallelCommandGroup(
                        new DriveToTargetFieldSide(
                                subsystemManager,
                                FieldSide.getChallengeFieldSide(autoChallengeType, allianceColor),
                                allianceColor,
                                waypointSeriesType),
                        new SetFlowerToolState(subsystemManager, DOWN)),
                new RunIntake(subsystemManager, INTAKE),
                collectFromFLower(autoChallengeType, allianceColor),
                new StopIntake(subsystemManager),
                new DriveToLaunchAndShoot(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        allowFieldCrossing,
                        abort),
                new SetFlowerToolState(subsystemManager, UP));
    }

    private SequentialCommandGroup collectFromFLower(
            AutoChallengeType autoChallengeType,
            AllianceColor allianceColor) {
        Pose flowerPose = getFlowerPose(autoChallengeType, allianceColor);
        return new SequentialCommandGroup(
                new DriveToPosition(subsystemManager, flowerPose, FLOWER_COLLECTION_MAX_SPEED)
                        .withTimeout(MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL),
                new WaitCommand(MILLIS_TO_WAIT_FOR_FLOWER_INTAKE),
                oscillateOnFlower(flowerPose),
                new DriveToPosition(
                        subsystemManager,
                        getFlowerBackupPose(autoChallengeType, allianceColor)));
    }

    private Pose getFlowerPose(AutoChallengeType autoChallengeType, AllianceColor allianceColor) {
        if (autoChallengeType == AutoChallengeType.PARK_SIDE_FLOWER) {
            return PARK_SIDE_FLOWER.getPose(allianceColor);
        } else {
            return HIVE_SIDE_FLOWER.getPose(allianceColor);
        }
    }

    private ParallelRaceGroup oscillateOnFlower(Pose flowerPose) {
        ParallelRaceGroup oscillationGroup = new ParallelRaceGroup();
        if (subsystemManager.getHuskyLensSubsystem() != null) {
            oscillationGroup.addCommands(
                    new WaitUntilCommand(
                            () -> subsystemManager.getHuskyLensSubsystem().getBlocksOfType(Constants.ArtifactType.POLLEN).length == 0));
        }
        oscillationGroup.addCommands(
                new WaitUntilCommand(
                        () -> subsystemManager.getHuskyLensSubsystem().getBlocksOfType(Constants.ArtifactType.POLLEN).length == 0),
                new RaceSafeSequentialCommandGroup(
                    new DriveToPosition(
                            subsystemManager,
                            flowerPose.withHeading(flowerPose.heading() + FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                            .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                    new DriveToPosition(
                            subsystemManager,
                            flowerPose.withHeading(flowerPose.heading() - FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                            .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                    new DriveToPosition(
                            subsystemManager,
                            flowerPose.withHeading(flowerPose.heading() + FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                            .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER),
                    new DriveToPosition(
                            subsystemManager,
                            flowerPose.withHeading(flowerPose.heading() - FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION))
                            .withTimeout(MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER)));
        return oscillationGroup;
    }

    private Pose getFlowerBackupPose(
            AutoChallengeType autoChallengeType, Constants.AllianceColor allianceColor) {
        Pose flowerPose = getFlowerPose(autoChallengeType, allianceColor);
        if (autoChallengeType == AutoChallengeType.PARK_SIDE_FLOWER) {
            if (isRed(allianceColor)) {
                return flowerPose.withX(flowerPose.x() + FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING);
            }
            return flowerPose.withX(flowerPose.x() - FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING);
        } else {
            if (isRed(allianceColor)) {
                return flowerPose.withY(flowerPose.y() - FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING);
            }
            return flowerPose.withY(flowerPose.y() + FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING);
        }
    }
}
