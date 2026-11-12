package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.pedropathing.math.Pose;

import java.util.function.Supplier;

public class DriveToPosition extends CommandBase {

    private final DriveSubsystem driveSubsystem;
    private final Pose pose;
    private final Supplier<Pose> poseSupplier;
    private final Double maxSpeed;

    public DriveToPosition(DriveSubsystem driveSubsystem, Pose pose) {
        this(driveSubsystem, pose, null);
    }

    public DriveToPosition(DriveSubsystem driveSubsystem, Pose pose, Double maxSpeed) {
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        this.poseSupplier = null;
        this.maxSpeed = maxSpeed;
        addRequirements(driveSubsystem);
    }

    public DriveToPosition(DriveSubsystem driveSubsystem, Supplier<Pose> poseSupplier) {
        this(driveSubsystem, poseSupplier, null);
    }

    public DriveToPosition(DriveSubsystem driveSubsystem, Supplier<Pose> poseSupplier, Double maxSpeed) {
        this.driveSubsystem = driveSubsystem;
        this.pose = null;
        this.poseSupplier = poseSupplier;
        this.maxSpeed = maxSpeed;
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        Pose targetPose = (poseSupplier != null) ? poseSupplier.get() : pose;
        driveSubsystem.driveToPosition(targetPose, maxSpeed);
    }

    @Override
    public boolean isFinished() {
        return !driveSubsystem.isBusy();
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            driveSubsystem.stop();
        }
    }
}