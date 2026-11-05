package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;

public class UpdateLauncher extends CommandBase {

    private final LaunchSubsystem launcherSubsystem;
    private final LimelightSubsystem limelightSubsystem;

    public UpdateLauncher(
            LaunchSubsystem launcherSubsystem, LimelightSubsystem limelightSubsystem) {
        this.launcherSubsystem = launcherSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        addRequirements(launcherSubsystem);
    }

    @Override
    public void execute() {
        launcherSubsystem.updateLauncherVelocity(
                limelightSubsystem.getTargetDistance());
    }
}
