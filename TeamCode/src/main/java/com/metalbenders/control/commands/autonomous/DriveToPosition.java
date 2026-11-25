package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.SubsystemManager;
import com.pedropathing.math.Pose;

import java.util.function.Supplier;

public class DriveToPosition extends CommandBase {

    private final SubsystemManager subsystemManager;
    private final Pose pose;
    private final Supplier<Pose> poseSupplier;
    private final Double maxSpeed;

    public DriveToPosition(SubsystemManager subsystemManager, Pose pose) {
        this(subsystemManager, pose, null);
    }

    public DriveToPosition(SubsystemManager subsystemManager, Pose pose, Double maxSpeed) {
        this.subsystemManager = subsystemManager;
        this.pose = pose;
        this.poseSupplier = null;
        this.maxSpeed = maxSpeed;
        addRequirements(subsystemManager.getDriveSubsystem());
    }

    public DriveToPosition(SubsystemManager subsystemManager, Supplier<Pose> poseSupplier) {
        this(subsystemManager, poseSupplier, null);
    }

    public DriveToPosition(SubsystemManager subsystemManager, Supplier<Pose> poseSupplier, Double maxSpeed) {
        this.subsystemManager = subsystemManager;
        this.pose = null;
        this.poseSupplier = poseSupplier;
        this.maxSpeed = maxSpeed;
        addRequirements(subsystemManager.getDriveSubsystem());
    }

    @Override
    public void initialize() {
        Pose targetPose = (poseSupplier != null) ? poseSupplier.get() : pose;
        subsystemManager.getDriveSubsystem().driveToPosition(targetPose, maxSpeed);
    }

    @Override
    public boolean isFinished() {
        return !subsystemManager.getDriveSubsystem().isBusy();
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            subsystemManager.getDriveSubsystem().stop();
        }
    }
}