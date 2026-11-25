package com.metalbenders.control.commands.launcher;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.Constants.LAUNCH_GATE_POSITION_DELAY_MILLI;

public class SetLaunchGateState extends ParallelDeadlineGroup {

    public SetLaunchGateState(SubsystemManager subsystemManager, Constants.LaunchGateState launchGateState) {
        super (
                new WaitCommand(LAUNCH_GATE_POSITION_DELAY_MILLI),
                new InstantCommand(
                        () -> subsystemManager.getLaunchSubsystem().setLaunchGateState(launchGateState),
                        subsystemManager.getLaunchSubsystem()));
    }
}
