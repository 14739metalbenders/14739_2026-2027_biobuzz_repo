package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class UpdateTurretPositionCommand extends CommandBase {

    private TurretSubsystem turretSubsystem;
    private LimelightSubsystem limelightSubsystem;
    private DriveSubsystem driveSubsystem;

    public UpdateTurretPositionCommand(
            TurretSubsystem turretSubsystem, LimelightSubsystem limelightSubsystem, DriveSubsystem driveSubsystem) {
        this.turretSubsystem = turretSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.driveSubsystem = driveSubsystem;
        addRequirements(turretSubsystem, limelightSubsystem, driveSubsystem);
    }

    @Override
    public void execute() {
    }
}
