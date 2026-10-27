package com.metalbenders.opmodes;

import com.metalbenders.autostrategies.AutoStrategyType;
import com.metalbenders.constants.AutoFieldBoundary;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Auto", group = "Auto", preselectTeleOp = "TeleOp")
public class AutoMode extends BaseOpMode {

    protected Constants.FieldSide startingFieldSide;
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
                lidarSubsystem);
    }
}
