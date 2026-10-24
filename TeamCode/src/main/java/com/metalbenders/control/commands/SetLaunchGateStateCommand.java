package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.LaunchSubsystem;

import static com.metalbenders.constants.Constants.LAUNCH_GATE_POSITION_DELAY_MILLI;

public class SetLaunchGateStateCommand extends CommandBase {

    private final LaunchSubsystem launchSubsystem;
    private final Constants.LaunchGateState launchGateState;
    private long startTime;

    public SetLaunchGateStateCommand(LaunchSubsystem launchSubsystem, Constants.LaunchGateState launchGateState) {
        this.launchSubsystem = launchSubsystem;
        this.launchGateState = launchGateState;
        addRequirements(launchSubsystem);
    }

    @Override
    public void initialize() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        launchSubsystem.setLaunchGateState(launchGateState);
    }

    @Override
    public boolean isFinished() {
        return (System.currentTimeMillis() - startTime) >= LAUNCH_GATE_POSITION_DELAY_MILLI;
    }
}
