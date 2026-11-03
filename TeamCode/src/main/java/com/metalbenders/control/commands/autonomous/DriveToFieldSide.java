package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.math.Pose;

public class DriveToFieldSide extends SequentialCommandGroup {

    public DriveToFieldSide(
            DriveSubsystem driveSubsystem,
            FieldSide currentFieldSide,
            FieldSide targetFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        if (currentFieldSide == targetFieldSide) {
            return;
        }
        addCommands(
                new DriveToPosition(
                        driveSubsystem,
                        getWaypointPose(currentFieldSide, allianceColor, useInsideWaypoints)),
                new DriveToPosition(
                        driveSubsystem,
                        getWaypointPose(targetFieldSide, allianceColor, useInsideWaypoints)));
    }

    private Pose getWaypointPose(FieldSide fieldSide, Constants.AllianceColor allianceColor, boolean useInsideWaypoints) {
        return useInsideWaypoints ? FieldLocation.getInnerWaypointLocation(fieldSide).getPose(allianceColor)
                : FieldLocation.getOuterWaypointLocation(fieldSide).getPose(allianceColor);
    }
}
