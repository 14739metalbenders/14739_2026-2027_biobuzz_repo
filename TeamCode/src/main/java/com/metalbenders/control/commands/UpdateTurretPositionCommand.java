package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class UpdateTurretPositionCommand extends CommandBase {

    private final TurretSubsystem turretSubsystem;
    private final LimelightSubsystem limelightSubsystem;
    private final DriveSubsystem driveSubsystem;

    public UpdateTurretPositionCommand(
            TurretSubsystem turretSubsystem, LimelightSubsystem limelightSubsystem, DriveSubsystem driveSubsystem) {
        this.turretSubsystem = turretSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.driveSubsystem = driveSubsystem;
        addRequirements(turretSubsystem, limelightSubsystem, driveSubsystem);
    }

    @Override
    public void execute() {
        turretSubsystem.determineTargetHeading(
                limelightSubsystem.getActiveHiveCellFieldSide(),
                driveSubsystem.getCurrentPose(),
                limelightSubsystem.getLastActiveSideDetections());
    }
}
