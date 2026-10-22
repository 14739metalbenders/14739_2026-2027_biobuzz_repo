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

    private SubsystemRegistry subsystemRegistry;
    @Override
    public void initialize() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        subsystemRegistry = new SubsystemRegistry(
                hardwareMap, telemetry, getAllianceColor(), getInitialPose(), getOpModeType());

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
        subsystemRegistry.closeAllSubsystems(getOpModeType());
        super.reset();
    }

    public SubsystemRegistry getSubsystemRegistry() {
        return subsystemRegistry;
    }
}
