package com.metalbenders.opmodes.auto;

import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.util.AutonomousStorage;

public abstract class BaseAuto extends BaseOpMode {
    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;

    @Override
    public void initialize() {
        driveSubsystem = new DriveSubsystem(getInitialPose(), telemetry);
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();
        AutonomousStorage.endPose = driveSubsystem.getFollower().pose();
    }
}
