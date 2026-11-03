package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.subsystems.DriveSubsystem;

import java.util.function.Supplier;

public class DriveToLaunch extends SequentialCommandGroup {

    public DriveToLaunch(
            DriveSubsystem driveSubsystem,
            Supplier<FieldSide> currentFieldSide,
            Supplier<FieldSide> activeFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        SelectCommand driveToLaunch = new SelectCommand(() -> {
            FieldSide currentSide = currentFieldSide.get();
            FieldSide activeSide = activeFieldSide.get();
            return new SequentialCommandGroup(
                    new DriveToFieldSide(
                            driveSubsystem,
                            currentSide,
                            activeSide,
                            allianceColor,
                            useInsideWaypoints),
                    new DriveToPosition(
                            driveSubsystem,
                            FieldLocation.getLaunchLocation(activeSide).getPose(allianceColor)));
        });
        // Supplier-based selection does not inherit the selected command's requirements.
        driveToLaunch.addRequirements(driveSubsystem);
        addCommands(driveToLaunch);
    }
}
