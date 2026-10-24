package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

public class SetIntakeStateCommand extends CommandBase {
    private final IntakeSubsystem intakeSubsystem;
    private final Constants.IntakeState intakeState;
    public SetIntakeStateCommand(
            IntakeSubsystem intakeSubsystem, Constants.IntakeState intakeState) {
        this.intakeSubsystem = intakeSubsystem;
        this.intakeState = intakeState;
        addRequirements(intakeSubsystem);
    }

    @Override
    public void execute() {
        intakeSubsystem.setIntakeState(intakeState);
    }

    @Override
    public boolean isFinished() {
        // INTAKE/REVERSE run while the button is held; STOP is a one-shot so sequences can continue.
        return intakeState == Constants.IntakeState.STOP;
    }

    @Override
    public void end(boolean interrupted) {
        if (intakeState == Constants.IntakeState.REVERSE_INTAKE || intakeState == Constants.IntakeState.INTAKE) {
            intakeSubsystem.setIntakeState(Constants.IntakeState.STOP);
        }
    }
}