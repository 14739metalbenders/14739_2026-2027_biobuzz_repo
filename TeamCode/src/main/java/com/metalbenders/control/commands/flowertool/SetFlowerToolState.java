package com.metalbenders.control.commands.flowertool;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.Constants.FLOWER_TOOL_POSITION_DELAY_MILLIS;

public class SetFlowerToolState extends ParallelDeadlineGroup {

    private final SubsystemManager subsystemManager;

    public SetFlowerToolState(SubsystemManager subsystemManager, Constants.FlowerToolState flowerToolState) {
        super (
                new WaitCommand(FLOWER_TOOL_POSITION_DELAY_MILLIS),
                new InstantCommand(
                        () -> subsystemManager.getFlowerToolSubsystem().setFlowerToolState(flowerToolState),
                        subsystemManager.getFlowerToolSubsystem()));
        this.subsystemManager = subsystemManager;
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        if (interrupted) {
            //Sub-commands were canceled mid-sequence, so put the hardware in a safe state directly
            subsystemManager.getFlowerToolSubsystem().setFlowerToolState(Constants.FlowerToolState.UP);
        }
    }
}
