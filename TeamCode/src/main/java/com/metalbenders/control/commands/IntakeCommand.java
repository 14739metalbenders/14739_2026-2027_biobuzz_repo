package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class IntakeCommand extends CommandBase {
    private final IntakeSubsystem intakeSubsystem;
    private final TurretSubsystem turretSubsystem;

    public IntakeCommand(IntakeSubsystem intakeSubsystem, TurretSubsystem turretSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        this.turretSubsystem = turretSubsystem;
        addRequirements(intakeSubsystem, turretSubsystem);
    }

    @Override
    public void execute() {
        turretSubsystem.closeLaunchGate();
        intakeSubsystem.intake();
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stop();
    }
}