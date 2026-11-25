package com.metalbenders.control.commands.turret;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.SubsystemManager;

public class UpdateTurret extends CommandBase {

    private final SubsystemManager subsystemManager;
    public UpdateTurret(SubsystemManager subsystemManager) {
        this.subsystemManager = subsystemManager;
        addRequirements(subsystemManager.getTurretSubsystem());
    }

    @Override
    public void execute() {
        subsystemManager.getTurretSubsystem().updateTurretPosition(
                subsystemManager.getDriveSubsystem().getCurrentTwist(),
                subsystemManager.getLimelightSubsystem().getAimError(),
                subsystemManager.getLaunchSubsystem().getLauncherRPM(),
                subsystemManager.getLimelightSubsystem().getTargetDistance(),
                subsystemManager.getLimelightSubsystem().isHiveDetected());
    }
}
