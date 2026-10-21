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

    private SubsystemMap subsystemMap = new SubsystemMap();
    @Override
    public void initialize() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        subsystemMap.register(DriveSubsystem.class, new DriveSubsystem(hardwareMap, telemetry, getInitialPose()));
        subsystemMap.register(IntakeSubsystem.class, new IntakeSubsystem(hardwareMap, telemetry));
        subsystemMap.register(LimelightSubsystem.class, new LimelightSubsystem(hardwareMap, telemetry, getAllianceColor()));
        subsystemMap.register(TurretSubsystem.class, new TurretSubsystem(hardwareMap, telemetry, getAllianceColor()));
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            subsystemMap.register(LidarSubsystem.class, new LidarSubsystem(hardwareMap, telemetry));
            subsystemMap.register(HuskyLensSubsystem.class, new HuskyLensSubsystem(hardwareMap, telemetry, getAllianceColor()));
        }

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
        for(CloseableSubsystem subsystem : subsystemMap.getAllSubsystems()) {
            subsystem.close(getOpModeType());
        }
        super.reset();
    }

    public SubsystemMap getSubsystemMap() {
        return subsystemMap;
    }
}
