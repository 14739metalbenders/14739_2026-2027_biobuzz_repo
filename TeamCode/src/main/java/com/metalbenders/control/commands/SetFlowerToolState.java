package com.metalbenders.control.commands;

import static com.metalbenders.constants.Constants.FLOWER_TOOL_POSITION_DELAY_MILLIS;

import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;

public class SetFlowerToolState extends WaitCommand {

    private final FlowerToolSubsystem flowerToolSubsystem;
    private final Constants.FlowerToolState flowerToolState;

    public SetFlowerToolState(FlowerToolSubsystem flowerToolSubsystem, Constants.FlowerToolState flowerToolState) {
        super(FLOWER_TOOL_POSITION_DELAY_MILLIS);
        this.flowerToolSubsystem = flowerToolSubsystem;
        this.flowerToolState = flowerToolState;
        addRequirements(flowerToolSubsystem);
    }

    @Override
    public void initialize() {
        flowerToolSubsystem.setFlowerToolState(flowerToolState);
        super.initialize();
    }
}
