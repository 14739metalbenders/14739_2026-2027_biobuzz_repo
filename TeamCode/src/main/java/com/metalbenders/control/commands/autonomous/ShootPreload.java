package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.SubsystemManager;

public class ShootPreload extends SequentialCommandGroup {

    public ShootPreload(
            SubsystemManager subsystemManager,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            boolean waitToShoot,
            boolean allowFieldCrossing,
            AutoAbort abort) {
        //drive to launch location on current field side
        new DriveToPosition(
                subsystemManager,
                () -> FieldLocation.getLaunchLocation(
                        FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose())).getPose(allianceColor));
        //wait for the hive to switch
        if (waitToShoot && allowFieldCrossing) {
            addCommands(new WaitForHiveSwitch(
                    subsystemManager, () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose())));
        }
        //drive to the launch location and shoot
        addCommands(
                new DriveToLaunchAndShoot(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        allowFieldCrossing,
                        abort));
    }
}
