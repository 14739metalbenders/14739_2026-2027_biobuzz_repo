package com.metalbenders.opmodes;

import com.metalbenders.constants.FieldSide;
import com.metalbenders.strategies.AutoStrategyType;
import com.metalbenders.constants.AutoFieldBoundary;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Auto", group = "Auto", preselectTeleOp = "Teleop")
public class Auto extends Base {

    protected FieldSide startingFieldSide;
    protected AutoStrategyType autoStrategyType;

    public AutoFieldBoundary getAutoFieldBoundary() {
        return AutoFieldBoundary.getByAllianceColor(allianceColor);
    }

    @Override
    public void initialize() {
        super.initialize();
        autoStrategyType.getStrategy().initialize(
                allianceColor,
                startingFieldSide,
                driveSubsystem,
                intakeSubsystem,
                launchSubsystem,
                turretSubsystem,
                huskyLensSubsystem,
                limelightSubsystem,
                lidarSubsystem,
                flowerToolSubsystem);
    }
}
