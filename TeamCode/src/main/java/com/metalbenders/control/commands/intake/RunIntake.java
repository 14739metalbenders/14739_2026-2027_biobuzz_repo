package com.metalbenders.control.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.IntakeSubsystem;

public class RunIntake extends InstantCommand {
    private final IntakeSubsystem intakeSubsystem;
    private final Constants.IntakeState intakeState;

    public RunIntake(IntakeSubsystem intakeSubsystem, Constants.IntakeState intakeState) {
        this.intakeSubsystem = intakeSubsystem;
        this.intakeState = intakeState;
        addRequirements(intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.setIntakeState(intakeState);
    }
}
