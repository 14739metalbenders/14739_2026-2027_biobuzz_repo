package com.metalbenders.opmodes;

import com.metalbenders.strategies.AutoStrategyType;
import com.metalbenders.constants.AutoFieldBoundary;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Auto", group = "Auto", preselectTeleOp = "TeleOp")
public class Auto extends Base {

    protected boolean startingOnActiveSide;
    protected AutoStrategyType autoStrategyType;
    private boolean strategyStarted;

    public AutoFieldBoundary getAutoFieldBoundary() {
        return AutoFieldBoundary.getByAllianceColor(allianceColor);
    }

    @Override
    public void initialize() {
        super.initialize();
        strategyStarted = false;
    }

    @Override
    public void run() {
        // Scheduling initializes commands immediately, so start the timer only after Start.
        if (!strategyStarted) {
            autoStrategyType.getStrategy().initialize(
                    allianceColor,
                    startingOnActiveSide,
                    driveSubsystem,
                    intakeSubsystem,
                    launchSubsystem,
                    turretSubsystem,
                    huskyLensSubsystem,
                    limelightSubsystem,
                    lidarSubsystem,
                    flowerToolSubsystem);
            strategyStarted = true;
        }
        super.run();
    }
}
