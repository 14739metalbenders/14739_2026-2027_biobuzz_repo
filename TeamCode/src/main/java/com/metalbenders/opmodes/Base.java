package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.UpdateLauncher;
import com.metalbenders.control.commands.UpdateTurret;
import com.metalbenders.control.subsystems.*;
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
    protected LightingSubsystem lightingSubsystem;
    protected LaunchSubsystem launchSubsystem;
    protected AllianceColor allianceColor;
    protected TurretSubsystem.TurretState turretState;

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
        limelightSubsystem = new LimelightSubsystem(hardwareMap, telemetry, allianceColor, getActiveHiveCellFieldSide());
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry, turretState);
        launchSubsystem = new LaunchSubsystem(hardwareMap, telemetry, allianceColor, getOpModeType());
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
        postInitializeStateCheck();
    }

    private void postInitializeStateCheck() {
        boolean errorDetected = false;
        if (getOpModeType() == AUTO) {
            if (lidarSubsystem.getLidarState() == Constants.LidarState.FAILED) {
                errorDetected = true;
            }
//            if (huskyLensSubsystem.getHuskyLensState() == Constants.HuskyLensState.FAILED) {
//                errorDetected = true;
//            }
        }
        if ( limelightSubsystem.getLimelightState() == Constants.LimelightState.FAILED) {
            errorDetected = true;
        }

        if (errorDetected) {
            lightingSubsystem.setCurrentAnimation(Constants.IndicatorAnimationEnum.BLINK);
        }
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
        telemetry.addData("Alliance Color", allianceColor);
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            Auto auto = (Auto) this;
            telemetry.addData("Starting on active side", auto.startingOnActiveSide);
            telemetry.addData("Auto Strategy", auto.autoStrategyType);
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
            //Store the end pose and active hive cell field side for use in teleop
            AutonomousStorage.currentActiveHiveSide = limelightSubsystem.getActiveHiveCellFieldSide();
            AutonomousStorage.endPose = driveSubsystem.getCurrentPose();

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
            return FieldLocation
                    .getStartLocation(
                            FieldSide.getStartingFieldSide(allianceColor, auto.startingOnActiveSide))
                    .getPose(allianceColor);
        } else {
            return AutonomousStorage.endPose == null ? DEFAULT_POSE : AutonomousStorage.endPose;
        }
    }

    public FieldSide getActiveHiveCellFieldSide() {
        if (getOpModeType() == Constants.OpModeType.TELEOP) {
            return AutonomousStorage.currentActiveHiveSide;
        }
        return FieldSide.getStartingHiveCellFieldSide(allianceColor);
    }

    private void initializeConfigurations() {
        ConfigFileUtil.ConfigFile configFile = ConfigFileUtil.readConfigFile(telemetry);
        allianceColor = configFile.getAllianceColor();
        turretState = configFile.getTurretState();
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            Auto auto = (Auto) this;
            auto.startingOnActiveSide = configFile.isStartingOnActiveSide();
            auto.autoStrategyType = configFile.getAutoStrategyType();
        }
    }
}
