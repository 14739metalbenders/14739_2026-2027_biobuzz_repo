package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class UpdateTurretCommand extends CommandBase {

    private final TurretSubsystem turretSubsystem;
    private final LimelightSubsystem limelightSubsystem;
    private final DriveSubsystem driveSubsystem;
    private final LaunchSubsystem launchSubsystem;

    public UpdateTurretCommand(
            TurretSubsystem turretSubsystem, LimelightSubsystem limelightSubsystem, DriveSubsystem driveSubsystem, LaunchSubsystem launchSubsystem) {
        this.turretSubsystem = turretSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.driveSubsystem = driveSubsystem;
        this.launchSubsystem = launchSubsystem;
        addRequirements(turretSubsystem);
    }

    @Override
    public void execute() {
        turretSubsystem.updateTurretPosition(
                limelightSubsystem.getActiveHiveCellFieldSide(),
                driveSubsystem.getCurrentPose(),
                driveSubsystem.getCurrentTwist(),
                limelightSubsystem.getLastActiveSideDetections(),
                limelightSubsystem.getAimError(),
                launchSubsystem.getLauncherRPM());
    }
}
