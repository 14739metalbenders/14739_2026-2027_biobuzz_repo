package com.metalbenders.opmodes.auto;

import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.util.AutonomousStorage;

public abstract class BaseAuto extends BaseOpMode {
    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private LimelightSubsystem limelightSubsystem;

    @Override
    public void initialize() {
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, getInitialPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
//        limelightSubsystem = new LimelightSubsystem(hardwareMap, telemetry, getAllianceColor());
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();
        AutonomousStorage.endPose = driveSubsystem.getFollower().pose();
    }
}
