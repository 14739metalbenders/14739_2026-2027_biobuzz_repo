package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class UpdateLauncherCommand extends CommandBase {

    private final LaunchSubsystem launcherSubsystem;
    private final LimelightSubsystem limelightSubsystem;
    private final DriveSubsystem driveSubsystem;

    public UpdateLauncherCommand(
            LaunchSubsystem launcherSubsystem, LimelightSubsystem limelightSubsystem, DriveSubsystem driveSubsystem) {
        this.launcherSubsystem = launcherSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.driveSubsystem = driveSubsystem;
        addRequirements(launcherSubsystem, limelightSubsystem, driveSubsystem);
    }

    @Override
    public void execute() {
        launcherSubsystem.updateLauncherVelocity(
                limelightSubsystem.getActiveHiveCellFieldSide(),
                driveSubsystem.getCurrentPose(),
                limelightSubsystem.getLastActiveSideDetections());
    }
}
