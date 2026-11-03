package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.commands.SetFlowerToolState;
import com.metalbenders.control.commands.SetIntakeState;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.pedropathing.math.Pose;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.FLOWER_INTAKE_TIMEOUT_MILLIS;
import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;

public class CollectFlowerPollen extends SequentialCommandGroup {

    public CollectFlowerPollen(
            FlowerToolSubsystem flowerToolSubsystem,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            FieldLocation flowerLocation,
            Supplier<FieldSide> currentFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        Pose flowerPose = flowerLocation.getPose(allianceColor);
        SelectCommand driveToFlowerSide = new SelectCommand(
                () -> new DriveToFieldSide(
                        driveSubsystem,
                        currentFieldSide.get(),
                        getTargetFieldSide(flowerLocation, allianceColor),
                        allianceColor,
                        useInsideWaypoints));
        // Supplier-based selection does not inherit the selected command's requirements.
        driveToFlowerSide.addRequirements(driveSubsystem);
        addCommands(
                driveToFlowerSide,
                new DriveToPosition(driveSubsystem, flowerPose),
                new SetFlowerToolState(flowerToolSubsystem, DOWN),
                new ParallelDeadlineGroup(
                        new DriveToPosition(
                                driveSubsystem,
                                new Pose(
                                        flowerPose.x() + getXDistanceToTravelToIntakeFromFlower(flowerLocation, allianceColor),
                                        flowerPose.y() + getYDistanceToTravelToIntakeFromFlower(flowerLocation, allianceColor),
                                        flowerPose.heading()))
                                .withTimeout(FLOWER_INTAKE_TIMEOUT_MILLIS),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new DriveToPosition(driveSubsystem, flowerPose),
                new SetFlowerToolState(flowerToolSubsystem, UP),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.STOP));
    }

    private FieldSide getTargetFieldSide(FieldLocation flowerLocation, Constants.AllianceColor allianceColor) {
        if (flowerLocation == FieldLocation.PARK_SIDE_FLOWER) {
            return FieldSide.getParkSideFlowerFieldSide(allianceColor);
        }
        return FieldSide.getOtherFlowerFieldSide(allianceColor);
    }

    private double getXDistanceToTravelToIntakeFromFlower(FieldLocation fieldLocation, Constants.AllianceColor allianceColor) {
        if (fieldLocation.name().startsWith("PARK_SIDE")) {
            return
                    allianceColor == Constants.AllianceColor.RED
                            ? -Constants.DISTANCE_TO_TRAVEL_TO_INTAKE_FROM_FLOWER
                            : Constants.DISTANCE_TO_TRAVEL_TO_INTAKE_FROM_FLOWER;
        }
        return 0;
    }

    private double getYDistanceToTravelToIntakeFromFlower(FieldLocation fieldLocation, Constants.AllianceColor allianceColor) {
        if (fieldLocation.name().startsWith("PARK_SIDE")) {
            return 0;
        }
        return
                allianceColor == Constants.AllianceColor.RED
                        ? Constants.DISTANCE_TO_TRAVEL_TO_INTAKE_FROM_FLOWER
                        : -Constants.DISTANCE_TO_TRAVEL_TO_INTAKE_FROM_FLOWER;
    }
}
