package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.math.Pose;

public class DriveToPositionCommand extends CommandBase {

    private final DriveSubsystem driveSubsystem;
    private final Pose pose;

    public DriveToPositionCommand(DriveSubsystem driveSubsystem, Pose pose) {
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        // Start the path via Pedro Follower exposed method
        driveSubsystem.driveToPosition(pose);
    }

    @Override
    public boolean isFinished() {
        // This command ends automatically when the robot arrives at the target
        return !driveSubsystem.isBusy();
    }
}