package com.metalbenders.opmodes.auto;

import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.util.AutonomousStorage;

public abstract class BaseAuto extends BaseOpMode {
    private DriveSubsystem driveSubsystem;

    @Override
    public void initialize() {
        driveSubsystem = new DriveSubsystem(getInitialPose());

    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();
        AutonomousStorage.endPose = driveSubsystem.getFollower().pose();
    }
}
