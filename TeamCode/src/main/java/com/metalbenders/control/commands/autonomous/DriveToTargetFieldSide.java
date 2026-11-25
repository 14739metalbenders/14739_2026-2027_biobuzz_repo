package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.SubsystemManager;

import java.util.function.Supplier;

public class DriveToTargetFieldSide extends ConditionalCommand {

    public DriveToTargetFieldSide(
            SubsystemManager subsystemManager,
            FieldSide targetFieldSide,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        this(
                subsystemManager,
                () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()),
                () -> targetFieldSide,
                allianceColor,
                waypointSeriesType);
    }

    public DriveToTargetFieldSide(
            SubsystemManager subsystemManager,
            Supplier<FieldSide> targetFieldSideSupplier,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        this(
                subsystemManager,
                () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()),
                targetFieldSideSupplier,
                allianceColor,
                waypointSeriesType);
    }

    public DriveToTargetFieldSide(
            SubsystemManager subsystemManager,
            Supplier<FieldSide> currentFieldSideSupplier,
            Supplier<FieldSide> targetFieldSideSupplier,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        super (
                new SequentialCommandGroup(
                        new DriveToPosition(
                                subsystemManager,
                                () -> FieldLocation.getWaypointLocation(
                                        currentFieldSideSupplier.get(), waypointSeriesType).getPose(allianceColor)),
                        new DriveToPosition(
                                subsystemManager,
                                () -> FieldLocation.getWaypointLocation(
                                        targetFieldSideSupplier.get(), waypointSeriesType).getPose(allianceColor))),
                new InstantCommand(),
                () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()) != targetFieldSideSupplier.get());
    }
}
