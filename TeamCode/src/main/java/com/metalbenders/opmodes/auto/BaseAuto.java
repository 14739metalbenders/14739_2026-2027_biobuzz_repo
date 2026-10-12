package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.util.AutonomousStorage;

public abstract class BaseAuto extends BaseOpMode {

    @Override
    public Constants.OpModeType getOpModeType() {
        return Constants.OpModeType.AUTO;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();
        AutonomousStorage.endPose = driveSubsystem.getCurrentPose();
    }
}
