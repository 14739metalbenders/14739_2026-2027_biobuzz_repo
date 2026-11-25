package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.launcher.UpdateLauncher;
import com.metalbenders.control.commands.turret.UpdateTurret;
import com.metalbenders.control.subsystems.SubsystemManager;
import com.metalbenders.util.AutonomousStorage;
import com.metalbenders.util.ConfigFileUtil;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;
import static com.metalbenders.constants.Constants.OpModeType.AUTO;

public abstract class Base extends CommandOpMode {

    protected GamepadEx driverGamepad;
    protected SubsystemManager subsystemManager;
    protected AllianceColor allianceColor;
    private AutonomousStorage.Handoff handoff;
    private boolean targetingInitialized;

    @Override
    public void initialize() {
        // Initialize the gamepad and telemetry
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        // Initialize configurations
        initializeConfigurations();
        handoff = getOpModeType() == AUTO ? null : AutonomousStorage.consume(allianceColor);
        targetingInitialized = false;

        // Initialize subsystems
        subsystemManager = new SubsystemManager(
                hardwareMap,
                telemetry,
                allianceColor,
                getStartingActiveHiveCellFieldSide(),
                getOpModeType(),
                getStartingPose());
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        if (!targetingInitialized) {
//            if (getOpModeType() != AUTO) {
                subsystemManager.getTurretSubsystem().setDefaultCommand(
                        new UpdateTurret(subsystemManager));
//            }
            subsystemManager.getLaunchSubsystem().setDefaultCommand(
                    new UpdateLauncher(subsystemManager));
            targetingInitialized = true;
        }
        super.run();
        subsystemManager.healthCheck();
        subsystemManager.updateReadinessIndicator();
        updateTelemetry(startTime);
    }

    private void updateTelemetry(long startTime) {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        updateRuntime();
        telemetry.addData("Main Loop Time", "%d ms", System.currentTimeMillis() - startTime);
        telemetry.addData("Alliance Color", allianceColor);
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            Auto auto = (Auto) this;
            telemetry.addData("Starting On Active Side", auto.startingOnActiveSide);
            telemetry.addData("Auto Strategy", auto.autoStrategy);
            telemetry.addData("Waypoint Series Type", auto.waypointSeriesType);
            telemetry.addData("Allow Field Crossing", auto.allowFieldCrossing);
            telemetry.addData("Active Hive Cell Field Side", auto.getStartingActiveHiveCellFieldSide());
        }
        telemetry.addLine();
        telemetry.update();
    }

    public void updateRuntime() {
        // Update the runtime telemetry in MM:SS format
        double totalSeconds = getRuntime();
        telemetry.addData(
                "Runtime",
                "%02.0f:%02.0f",
                totalSeconds / 60,
                totalSeconds % 60);
    }

    @Override
    public void reset() {
        subsystemManager.tearDown(targetingInitialized);
        super.reset();
    }

    public Constants.OpModeType getOpModeType() {
        if (this instanceof Auto) {
            return Constants.OpModeType.AUTO;
        }
        return Constants.OpModeType.TELEOP;
    }

    public Pose getStartingPose() {
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            Auto auto = (Auto) this;
            return FieldLocation.getStartLocation(
                            FieldSide.getRobotStartingFieldSide(allianceColor, auto.startingOnActiveSide))
                    .getPose(allianceColor);
        }
        return DEFAULT_POSE;
    }

    private void initializeConfigurations() {
        ConfigFileUtil.ConfigFile configFile = ConfigFileUtil.readConfigFile(telemetry);
        allianceColor = configFile.getAllianceColor();
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            Auto auto = (Auto) this;
            auto.startingOnActiveSide = configFile.isStartingOnActiveSide();
            auto.autoStrategy = configFile.getAutoStrategy();
            auto.waypointSeriesType = configFile.getWaypointSeriesType();
            auto.allowFieldCrossing = configFile.isFieldCrossingAllowed();
        }
    }

    public FieldSide getStartingActiveHiveCellFieldSide() {
        if (getOpModeType() == Constants.OpModeType.TELEOP) {
            return handoff != null && handoff.hiveSide != null ? handoff.hiveSide : FieldSide.SCORING;
        }
        return FieldSide.getStartingHiveCellFieldSide(allianceColor);
    }
}
