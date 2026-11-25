package com.metalbenders.control.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemManager;

public class StopIntake extends InstantCommand {
    private final SubsystemManager subsystemManager;

    public StopIntake(SubsystemManager subsystemManager) {
        this.subsystemManager = subsystemManager;
        addRequirements(subsystemManager.getIntakeSubsystem());
    }

    @Override
    public void initialize() {
        subsystemManager.getIntakeSubsystem().setIntakeState(Constants.IntakeState.STOP);
    }
}
