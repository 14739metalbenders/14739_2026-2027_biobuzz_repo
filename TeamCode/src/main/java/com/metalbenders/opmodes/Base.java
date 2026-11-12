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
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.FlowerToolSubsystem;
import com.metalbenders.control.subsystems.HuskyLensSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LidarSubsystem;
import com.metalbenders.control.subsystems.LightingSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;
import com.metalbenders.util.AutonomousStorage;
import com.metalbenders.util.ConfigFileUtil;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;
import static com.metalbenders.constants.Constants.OpModeType.AUTO;

public abstract class Base extends CommandOpMode {

    protected GamepadEx driverGamepad;
    protected DriveSubsystem driveSubsystem;
    protected IntakeSubsystem intakeSubsystem;
    protected LimelightSubsystem limelightSubsystem;
    protected TurretSubsystem turretSubsystem;
    protected LidarSubsystem lidarSubsystem;
    protected HuskyLensSubsystem huskyLensSubsystem;
    protected FlowerToolSubsystem flowerToolSubsystem;
    protected LaunchSubsystem launchSubsystem;
    protected LightingSubsystem lightingSubsystem;
    protected AllianceColor allianceColor;

    @Override
    public void initialize() {
        // Initialize the gamepad and telemetry
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        // Initialize configurations
        initializeConfigurations();

        // Initialize subsystems
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, getStartingPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
        limelightSubsystem = new LimelightSubsystem(
                hardwareMap, telemetry, allianceColor, getStartingActiveHiveCellFieldSide());
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry);
        launchSubsystem = new LaunchSubsystem(hardwareMap, telemetry, getOpModeType());
        flowerToolSubsystem = new FlowerToolSubsystem(hardwareMap, telemetry);
        lightingSubsystem = new LightingSubsystem(hardwareMap, telemetry, allianceColor);

        if (getOpModeType() == Constants.OpModeType.AUTO) {
            lidarSubsystem = new LidarSubsystem(hardwareMap, telemetry);
//            huskyLensSubsystem = new HuskyLensSubsystem(hardwareMap, telemetry, allianceColor);
        } else {
            turretSubsystem.setDefaultCommand(new UpdateTurret(
                    turretSubsystem, limelightSubsystem, driveSubsystem, launchSubsystem));

            launchSubsystem.setDefaultCommand(new UpdateLauncher(
                    launchSubsystem, limelightSubsystem));
        }
        subsystemHealthCheck();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        super.run();
        subsystemHealthCheck();
        updateTelemetry(startTime);
    }

    private void subsystemHealthCheck() {
        boolean errorDetected = !limelightSubsystem.isHealthy();
        if (!errorDetected && getOpModeType() == AUTO) {
            if (lidarSubsystem != null && !lidarSubsystem.isHealthy()) {
                errorDetected = true;
            } else if (huskyLensSubsystem != null && !huskyLensSubsystem.isHealthy()) {
                errorDetected = true;
            }
        }
        if (errorDetected) {
            lightingSubsystem.setCurrentAnimation(Constants.IndicatorAnimationEnum.BLINK);
        } else {
            lightingSubsystem.setCurrentAnimation(Constants.IndicatorAnimationEnum.SOLID);
        }
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
        if (getOpModeType() == AUTO) {
            //Store the end pose and alliance color for use in teleop
            AutonomousStorage.endPose = driveSubsystem.getCurrentPose();
            AutonomousStorage.currentActiveHiveSide = limelightSubsystem.getActiveHiveCellFieldSide();

            //teardown subsystems that are only used in autonomous
            lidarSubsystem.teardown();
//            huskyLensSubsystem.teardown();
        }
        //teardown subsystems that are used in both autonomous and teleop
        intakeSubsystem.teardown();
        limelightSubsystem.teardown();
        turretSubsystem.teardown();
        launchSubsystem.teardown();
        driveSubsystem.teardown();
        flowerToolSubsystem.teardown();
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
        }
    }

    public FieldSide getStartingActiveHiveCellFieldSide() {
        if (getOpModeType() == Constants.OpModeType.TELEOP) {
            return AutonomousStorage.currentActiveHiveSide;
        }
        return FieldSide.getStartingHiveCellFieldSide(allianceColor);
    }
}
