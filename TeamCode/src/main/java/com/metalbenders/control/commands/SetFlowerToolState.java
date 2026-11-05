package com.metalbenders.control.commands;

import static com.metalbenders.constants.Constants.FLOWER_TOOL_POSITION_DELAY_MILLIS;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;

public class SetFlowerToolState extends ParallelDeadlineGroup {

    public SetFlowerToolState(FlowerToolSubsystem flowerToolSubsystem, Constants.FlowerToolState flowerToolState) {
        super(
                new WaitCommand(FLOWER_TOOL_POSITION_DELAY_MILLIS),
                new InstantCommand(
                        () -> flowerToolSubsystem.setFlowerToolState(flowerToolState),
                        flowerToolSubsystem));
    }
}
