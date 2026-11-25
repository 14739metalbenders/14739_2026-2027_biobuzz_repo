package com.metalbenders.control.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemManager;

public class RunIntake extends InstantCommand {
    private final SubsystemManager subsystemManager;
    private final Constants.IntakeState intakeState;

    public RunIntake(SubsystemManager subsystemManager, Constants.IntakeState intakeState) {
        this.subsystemManager = subsystemManager;
        this.intakeState = intakeState;
        addRequirements(subsystemManager.getIntakeSubsystem());
    }

    @Override
    public void initialize() {
        subsystemManager.getIntakeSubsystem().setIntakeState(intakeState);
    }
}
