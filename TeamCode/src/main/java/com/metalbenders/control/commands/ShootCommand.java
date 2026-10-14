package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

import static com.metalbenders.control.subsystems.TurretSubsystem.LaunchGateState.CLOSED;

public class ShootCommand extends CommandBase {

    private final TurretSubsystem turretSubsystem;
    private final IntakeSubsystem intakeSubsystem;

    public ShootCommand(TurretSubsystem turretSubsystem, IntakeSubsystem intakeSubsystem) {
        this.turretSubsystem = turretSubsystem;
        this.intakeSubsystem = intakeSubsystem;
        addRequirements(turretSubsystem, intakeSubsystem);
    }

    @Override
    public void execute() {
        if (turretSubsystem.atLaunchVelocity()) {
            turretSubsystem.openLaunchGate();
            intakeSubsystem.intake();
        }
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stop();
        turretSubsystem.closeLaunchGate();
    }
}
