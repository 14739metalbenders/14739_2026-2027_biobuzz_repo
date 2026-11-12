package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.DriveSubsystem;

import static com.metalbenders.constants.AutoFieldBoundary.UNDER_HIVE_FIELD_BOUNDARY;

public class AutoPark extends SequentialCommandGroup {

    public AutoPark(
            DriveSubsystem driveSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType) {
        super (
                new ConditionalCommand(
                        new DriveToPosition(
                                driveSubsystem,
                                () -> FieldLocation.getWaypointLocation(
                                        FieldSide.getParkFieldSide(allianceColor), waypointSeriesType).getPose(allianceColor)),
                        new InstantCommand(),
                        () -> UNDER_HIVE_FIELD_BOUNDARY.isPoseWithinBoundary(driveSubsystem.getCurrentPose())),
                new DriveToTargetFieldSide(
                        driveSubsystem,
                        () -> FieldSide.getParkFieldSide(allianceColor),
                        allianceColor,
                        waypointSeriesType),
                new DriveToPosition(driveSubsystem, FieldLocation.getParkLocation().getPose(allianceColor)));
    }
}
