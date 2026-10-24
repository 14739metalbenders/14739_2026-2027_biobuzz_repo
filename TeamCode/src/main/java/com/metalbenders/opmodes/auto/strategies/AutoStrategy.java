package com.metalbenders.opmodes.auto.strategies;

import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.*;
import com.pedropathing.math.Pose;

public abstract class AutoStrategy {

    public abstract void initialize(
            Constants.AllianceColor allianceColor,
            Pose parkPose,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem);

    public abstract Constants.FieldSide getStartingFieldSide();
}
