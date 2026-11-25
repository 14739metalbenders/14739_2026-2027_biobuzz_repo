package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.launcher.UpdateLauncher;
import com.metalbenders.control.commands.turret.UpdateTurret;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.AutoFieldBoundary.inLaunchArea;
import static com.metalbenders.constants.Constants.AUTO_LAUNCH_LIDAR_HALF_RANGE_DEGREES;
import static com.metalbenders.constants.Constants.AUTO_LAUNCH_LIDAR_REAR_DEGREES;
import static com.metalbenders.constants.Constants.AUTO_LAUNCH_OBSTACLE_DISTANCE_MM;

public class DriveToLaunchAndShoot extends SequentialCommandGroup {

    public DriveToLaunchAndShoot(
            SubsystemManager subsystemManager,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            boolean allowFieldCrossing,
            AutoAbort abort) {

        UpdateTurret updateTurret = new UpdateTurret(subsystemManager);
        UpdateLauncher updateLauncher = new UpdateLauncher(subsystemManager);
        addCommands(
                new StopOnObstacle(
                        driveToActiveHiveCell(subsystemManager, allianceColor, waypointSeriesType, allowFieldCrossing),
                        () -> subsystemManager.getLidarSubsystem().isObstacleWithinDistanceBetweenRanges(
                                AUTO_LAUNCH_OBSTACLE_DISTANCE_MM,
                                AUTO_LAUNCH_LIDAR_REAR_DEGREES - AUTO_LAUNCH_LIDAR_HALF_RANGE_DEGREES,
                                // The Lidar range end is exclusive.
                                AUTO_LAUNCH_LIDAR_REAR_DEGREES + AUTO_LAUNCH_LIDAR_HALF_RANGE_DEGREES + 1),
                        () -> subsystemManager.getDriveSubsystem().stop()),

                allowFieldCrossing
                        ? new AutoShoot(subsystemManager, abort)
                        : NoCrossShooting.create(
                                () -> inLaunchArea(subsystemManager.getDriveSubsystem().getCurrentPose()),
                                new WaitForVisibleHive(
                                        () -> subsystemManager.getLimelightSubsystem().isHiveDetected(),
                                        () -> {
                                            // The enclosing strategy owns these subsystems, so their defaults cannot run.
                                            updateLauncher.execute();
                                            updateTurret.execute();
                                        },
                                        () -> subsystemManager.getTurretSubsystem().stop(),
                                        500),
                                new AutoShoot(subsystemManager, abort.requestCommand(),
                                        () -> subsystemManager.getLimelightSubsystem().isHiveDetected(),
                                        false),
                                abort));
    }

    private SequentialCommandGroup driveToActiveHiveCell(
            SubsystemManager subsystemManager,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            boolean allowFieldCrossing) {
        SequentialCommandGroup drive = new SequentialCommandGroup(
                //drive to launch location on current field side (to ensure visibility of the hive)
                new DriveToPosition(
                        subsystemManager,
                        () -> FieldLocation
                                .getLaunchLocation(FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()))
                                .getPose(allianceColor)));

        if (allowFieldCrossing) {
            drive.addCommands(
                    //check if hive is visible, if not drive to the opposite field side and then drive to the launch location
                    new ConditionalCommand(
                            new InstantCommand(),
                            new SequentialCommandGroup(
                                    new DriveToTargetFieldSide(
                                            subsystemManager,
                                            () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()).getOppositeSide(),
                                            allianceColor,
                                            waypointSeriesType),
                                    new DriveToPosition(
                                            subsystemManager,
                                            () -> FieldLocation
                                                    .getLaunchLocation(FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose()))
                                                    .getPose(allianceColor))),
                            () -> {
                                if (!subsystemManager.getLimelightSubsystem().isHiveDetected()) {
                                    return false;
                                }
                                Constants.HiveState hiveState = subsystemManager.getLimelightSubsystem().getHiveState();
                                //should return true regardless of whether it is transitioning to or already active on the current field side
                                return hiveState.getFieldSide() == FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose());
                            }));
        }
        return drive;
    }
}