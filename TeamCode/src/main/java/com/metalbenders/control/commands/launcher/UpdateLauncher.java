package com.metalbenders.control.commands.launcher;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.SubsystemManager;

public class UpdateLauncher extends CommandBase {

    private final SubsystemManager subsystemManager;
    public UpdateLauncher(SubsystemManager subsystemManager) {
        this.subsystemManager = subsystemManager;
        addRequirements(subsystemManager.getLaunchSubsystem());
    }

    @Override
    public void execute() {
        subsystemManager.getLaunchSubsystem().updateLauncherVelocity(
                subsystemManager.getLimelightSubsystem().getTargetDistance());
    }
}
