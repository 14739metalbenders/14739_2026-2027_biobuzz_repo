package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class ShootCommand extends CommandBase {

    private TurretSubsystem turretSubsystem;

    public ShootCommand(TurretSubsystem turretSubsystem) {
        this.turretSubsystem = turretSubsystem;
        addRequirements(turretSubsystem);
    }

    @Override
    public void execute() {
        turretSubsystem.setLauncherPower(.58);
    }

    @Override
    public void end(boolean interrupted) {
        turretSubsystem.setLauncherPower(.0);
    }
}
