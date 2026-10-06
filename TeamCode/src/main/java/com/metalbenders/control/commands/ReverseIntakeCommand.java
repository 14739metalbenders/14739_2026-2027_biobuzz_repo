package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.IntakeSubsystem;

public class ReverseIntakeCommand extends CommandBase {
    private final IntakeSubsystem intakeSubsystem;

    public ReverseIntakeCommand(IntakeSubsystem intakeSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        addRequirements(intakeSubsystem);
    }

    @Override
    public void execute() {
       intakeSubsystem.reverseIntake();
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stop();
    }
}