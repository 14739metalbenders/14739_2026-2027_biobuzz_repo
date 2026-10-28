package com.metalbenders.control.commands;

import static com.metalbenders.constants.Constants.LAUNCH_GATE_POSITION_DELAY_MILLI;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.LaunchSubsystem;

public class SetLaunchGateStateCommand extends ParallelDeadlineGroup {

    public SetLaunchGateStateCommand(LaunchSubsystem launchSubsystem, Constants.LaunchGateState launchGateState) {
        super(
                new WaitCommand(LAUNCH_GATE_POSITION_DELAY_MILLI),
                new InstantCommand(
                        () -> launchSubsystem.setLaunchGateState(launchGateState),
                        launchSubsystem));
    }
}
