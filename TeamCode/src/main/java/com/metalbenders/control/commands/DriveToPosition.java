package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.math.Pose;

public class DriveToPosition extends CommandBase {

    private final DriveSubsystem driveSubsystem;
    private final Pose pose;
    private final Double maxSpeed;

    public DriveToPosition(DriveSubsystem driveSubsystem, Pose pose) {
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        this.maxSpeed = null;
        addRequirements(driveSubsystem);
    }

    public DriveToPosition(DriveSubsystem driveSubsystem, Pose pose, Double maxSpeed) {
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        this.maxSpeed = maxSpeed;
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        // Start the path via Pedro Follower exposed method
        driveSubsystem.driveToPosition(pose, maxSpeed);
    }

    @Override
    public boolean isFinished() {
        // This command ends automatically when the robot arrives at the target
        return !driveSubsystem.isBusy();
    }

    @Override
    public void end(boolean interrupted) {
        // Stop the drive subsystem when the command ends
        if (interrupted) {
            driveSubsystem.stop();
        }
    }
}