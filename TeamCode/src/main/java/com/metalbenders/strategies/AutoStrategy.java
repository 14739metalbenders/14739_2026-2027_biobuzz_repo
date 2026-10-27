package com.metalbenders.strategies;

import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.*;

public abstract class AutoStrategy {

    protected Constants.FieldSide startingFieldSide;
    protected Constants.AllianceColor allianceColor;

    public void initialize(
            Constants.AllianceColor allianceColor,
            Constants.FieldSide startingFieldSide,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem) {
        this.startingFieldSide = startingFieldSide;
        this.allianceColor = allianceColor;

        initializeAuto(
                driveSubsystem,
                intakeSubsystem,
                launchSubsystem,
                turretSubsystem,
                huskyLensSubsystem,
                limelightSubsystem,
                lidarSubsystem,
                startingOnActiveSide());
    }

    protected abstract void initializeAuto(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem,
            boolean startingOnActiveSide);

    public boolean startingOnActiveSide() {
        if (allianceColor == Constants.AllianceColor.RED) {
            return startingFieldSide == Constants.FieldSide.AUDIENCE_SIDE;
        } else {
            return startingFieldSide == Constants.FieldSide.SCORE_SIDE;
        }
    }
}
