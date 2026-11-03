package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.subsystems.DriveSubsystem;

import java.util.function.Supplier;

public class AutoPark extends SequentialCommandGroup {

    public AutoPark(
            DriveSubsystem driveSubsystem,
            Supplier<FieldSide> currentFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        SelectCommand park = new SelectCommand(() -> {
            FieldSide currentFieldSideValue = currentFieldSide.get();
            FieldSide parkFieldSide = FieldSide.getParkFieldSide(allianceColor);
            SequentialCommandGroup route = new SequentialCommandGroup();
            if (currentFieldSideValue != parkFieldSide) {
                route.addCommands(
                        new DriveToFieldSide(
                                driveSubsystem,
                                currentFieldSideValue,
                                parkFieldSide,
                                allianceColor,
                                useInsideWaypoints));
            } else {
                route.addCommands(new DriveToPosition(
                        driveSubsystem,
                        FieldLocation.getOuterWaypointLocation(currentFieldSideValue).getPose(allianceColor)));
            }
            route.addCommands(
                    new DriveToPosition(
                            driveSubsystem,
                            FieldLocation.getParkLocation(parkFieldSide).getPose(allianceColor)));
            return route;
        });
        // Supplier-based selection does not inherit the selected command's requirements.
        park.addRequirements(driveSubsystem);
        addCommands(park);
    }
}
