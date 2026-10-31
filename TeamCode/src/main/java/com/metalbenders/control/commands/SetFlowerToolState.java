package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;

public class SetFlowerToolState extends CommandBase {

    private FlowerToolSubsystem flowerToolSubsystem;
    private Constants.FlowerToolState flowerToolState;

    public SetFlowerToolState(FlowerToolSubsystem flowerToolSubsystem, Constants.FlowerToolState flowerToolState) {
        this.flowerToolSubsystem = flowerToolSubsystem;
        this.flowerToolState = flowerToolState;
        addRequirements(flowerToolSubsystem);
    }

    @Override
    public void execute() {
        flowerToolSubsystem.setFlowerToolState(flowerToolState);
    }
}
