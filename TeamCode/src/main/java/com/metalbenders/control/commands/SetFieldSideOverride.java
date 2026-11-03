package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.LimelightSubsystem;

public class SetFieldSideOverride extends CommandBase {

    private final FieldSide fieldSideOverride;
    private final LimelightSubsystem limelightSubsystem;

    public SetFieldSideOverride(LimelightSubsystem limelightSubsystem, FieldSide fieldSideOverride) {
        this.limelightSubsystem = limelightSubsystem;
        this.fieldSideOverride = fieldSideOverride;
        addRequirements(limelightSubsystem);
    }

    @Override
    public void execute() {
        limelightSubsystem.setFieldSideOverride(fieldSideOverride);
    }
}
