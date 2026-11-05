package com.metalbenders.strategies;

import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.*;

public abstract class AutoStrategy {

    protected FieldSide startingFieldSide;
    protected FieldSide oppositeFieldSide;
    protected Constants.AllianceColor allianceColor;

    public void initialize(
            Constants.AllianceColor allianceColor,
            FieldSide startingFieldSide,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem,
            FlowerToolSubsystem flowerToolSubsystem) {
        this.startingFieldSide = startingFieldSide;
        this.oppositeFieldSide = startingFieldSide.getOppositeSide();
        this.allianceColor = allianceColor;

        initializeAuto(
                driveSubsystem,
                intakeSubsystem,
                launchSubsystem,
                turretSubsystem,
                huskyLensSubsystem,
                limelightSubsystem,
                lidarSubsystem,
                flowerToolSubsystem,
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
            FlowerToolSubsystem flowerToolSubsystem,
            boolean startingOnActiveSide);

    public boolean startingOnActiveSide() {
        if (allianceColor == Constants.AllianceColor.RED) {
            return startingFieldSide == FieldSide.AUDIENCE_SIDE;
        } else {
            return startingFieldSide == FieldSide.SCORE_SIDE;
        }
    }
}
