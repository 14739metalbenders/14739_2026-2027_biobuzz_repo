package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.paths.Path;

public class FollowPathCommand extends CommandBase {
    private final DriveSubsystem drive;
    private final Path path;

    public FollowPathCommand(DriveSubsystem drive, Path path) {
        this.drive = drive;
        this.path = path;
        addRequirements(drive);
    }

    @Override
    public void initialize() {
        // Start the path via Pedro Follower exposed method
        drive.getFollower().follow(path);
    }

    @Override
    public boolean isFinished() {
        // This command ends automatically when the robot arrives at the target
        return !drive.getFollower().isBusy();
    }
}