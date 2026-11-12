package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.DriveSubsystem;

import java.util.function.Supplier;

public class DriveToTargetFieldSide extends ConditionalCommand {

    public DriveToTargetFieldSide(
            DriveSubsystem driveSubsystem,
            FieldSide targetFieldSide,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        this(
                driveSubsystem,
                () -> FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose()),
                () -> targetFieldSide,
                allianceColor,
                waypointSeriesType);
    }


    public DriveToTargetFieldSide(
            DriveSubsystem driveSubsystem,
            Supplier<FieldSide> targetFieldSideSupplier,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        this(
                driveSubsystem,
                () -> FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose()),
                targetFieldSideSupplier,
                allianceColor,
                waypointSeriesType);
    }

    public DriveToTargetFieldSide(
            DriveSubsystem driveSubsystem,
            Supplier<FieldSide> currentFieldSideSupplier,
            Supplier<FieldSide> targetFieldSideSupplier,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        super (
                new SequentialCommandGroup(
                        new DriveToPosition(
                                driveSubsystem,
                                () -> FieldLocation.getWaypointLocation(
                                        currentFieldSideSupplier.get(), waypointSeriesType).getPose(allianceColor)),
                        new DriveToPosition(
                                driveSubsystem,
                                () -> FieldLocation.getWaypointLocation(
                                        targetFieldSideSupplier.get(), waypointSeriesType).getPose(allianceColor))),
                new InstantCommand(),
                () -> FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose()) != targetFieldSideSupplier.get());
    }
}
