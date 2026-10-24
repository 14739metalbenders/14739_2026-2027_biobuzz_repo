package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.control.subsystems.*;
import com.pedropathing.math.Pose;

public abstract class BaseOpMode extends CommandOpMode {

    public abstract AllianceColor getAllianceColor();
    public abstract Pose getInitialPose();
    public abstract Constants.OpModeType getOpModeType();

    protected DriveSubsystem driveSubsystem;
    protected IntakeSubsystem intakeSubsystem;
    protected LimelightSubsystem limelightSubsystem;
    protected TurretSubsystem turretSubsystem;
    protected LidarSubsystem lidarSubsystem;
    protected HuskyLensSubsystem huskyLensSubsystem;
    protected LaunchSubsystem launchSubsystem;
    @Override
    public void initialize() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, getInitialPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
        limelightSubsystem = new LimelightSubsystem(hardwareMap, telemetry, getAllianceColor());
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry, getAllianceColor());
        launchSubsystem = new LaunchSubsystem(hardwareMap, telemetry, getAllianceColor());
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            lidarSubsystem = new LidarSubsystem(hardwareMap, telemetry);
            huskyLensSubsystem = new HuskyLensSubsystem(hardwareMap, telemetry, getAllianceColor());

        }
//        turretSubsystem.setDefaultCommand(new UpdateLauncherCommand(
//                turretSubsystem, limelightSubsystem, driveSubsystem));

//        turretSubsystem.setDefaultCommand(new UpdateLauncherCommand(
//                turretSubsystem, limelightSubsystem, driveSubsystem));
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        super.run();
        updateTelemetry(startTime);
    }

    private void updateTelemetry(long startTime) {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        updateRuntime();
        telemetry.addData("Main Loop Time", "%d ms", System.currentTimeMillis() - startTime);
        telemetry.addData("Alliance Color", getAllianceColor());
        telemetry.addLine();
        telemetry.update();
    }

    public void updateRuntime() {
        double totalSeconds = getRuntime();
        telemetry.addData(
                "Runtime",
                "%02.0f:%02.0f",
                totalSeconds / 60,
                totalSeconds % 60);
    }

    @Override
    public void reset() {
        intakeSubsystem.teardown();
        limelightSubsystem.teardown();
        turretSubsystem.teardown();
        launchSubsystem.teardown();
        driveSubsystem.teardown(getOpModeType());
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            lidarSubsystem.teardown();
        }
        super.reset();
    }
}
