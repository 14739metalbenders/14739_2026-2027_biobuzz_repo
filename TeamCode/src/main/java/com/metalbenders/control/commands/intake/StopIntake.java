package com.metalbenders.control.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.IntakeSubsystem;

public class StopIntake extends InstantCommand {
    private final IntakeSubsystem intakeSubsystem;

    public StopIntake(IntakeSubsystem intakeSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        addRequirements(intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.setIntakeState(Constants.IntakeState.STOP);
    }
}
