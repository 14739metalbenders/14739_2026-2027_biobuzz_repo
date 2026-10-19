package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.LimelightSubsystem;

public class SetFieldSideOverrideCommand extends CommandBase {

    private final Constants.FieldSide fieldSideOverride;
    private final LimelightSubsystem limelightSubsystem;

    public SetFieldSideOverrideCommand(LimelightSubsystem limelightSubsystem, Constants.FieldSide fieldSideOverride) {
        this.limelightSubsystem = limelightSubsystem;
        this.fieldSideOverride = fieldSideOverride;
        addRequirements(limelightSubsystem);
    }

    @Override
    public void execute() {
        limelightSubsystem.setFieldSideOverride(fieldSideOverride);
    }
}
