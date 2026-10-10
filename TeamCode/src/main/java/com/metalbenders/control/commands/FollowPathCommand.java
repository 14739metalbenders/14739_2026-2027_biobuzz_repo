package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.paths.Path;

public class FollowPathCommand extends CommandBase {
    private final DriveSubsystem driveSubsystem;
    private final Path path;

    public FollowPathCommand(DriveSubsystem driveSubsystem, Path path) {
        this.driveSubsystem = driveSubsystem;
        this.path = path;
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        // Start the path via Pedro Follower exposed method
        driveSubsystem.followPath(path);
    }

    @Override
    public boolean isFinished() {
        // This command ends automatically when the robot arrives at the target
        return !driveSubsystem.isBusy();
    }
}