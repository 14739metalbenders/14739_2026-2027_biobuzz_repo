package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.autostrategies.AutoStrategyType;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.control.subsystems.*;
import com.metalbenders.util.AutonomousStorage;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;
import static com.metalbenders.constants.Constants.OpModeType.AUTO;
import static com.metalbenders.constants.FieldLocation.AUDIENCE_SIDE_START_POSE;
import static com.metalbenders.constants.FieldLocation.SCORE_SIDE_START_POSE;

public abstract class BaseOpMode extends CommandOpMode {

    protected GamepadEx driverGamepad;
    protected DriveSubsystem driveSubsystem;
    protected IntakeSubsystem intakeSubsystem;
    protected LimelightSubsystem limelightSubsystem;
    protected TurretSubsystem turretSubsystem;
    protected LidarSubsystem lidarSubsystem;
//    protected HuskyLensSubsystem huskyLensSubsystem;
    protected LaunchSubsystem launchSubsystem;
    protected AllianceColor allianceColor;

    @Override
    public void initialize() {
        // Initialize the gamepad and telemetry
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        if (getOpModeType() == Constants.OpModeType.AUTO) {
            AutoMode autoMode = (AutoMode) this;
            autoMode.startingFieldSide = promptForStartingFieldSide();
            autoMode.autoStrategyType = promptForAutoStrategy();
        } else {
            // For teleop, set the alliance color based on the stored value from autonomous
            allianceColor = AutonomousStorage.allianceColor;
            allianceColor = promptForAllianceColor();
        }

        // Initialize subsystems
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, getStartingPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
        limelightSubsystem = new LimelightSubsystem(hardwareMap, telemetry, allianceColor);
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry, allianceColor);
        launchSubsystem = new LaunchSubsystem(hardwareMap, telemetry, allianceColor);
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            lidarSubsystem = new LidarSubsystem(hardwareMap, telemetry);
//            huskyLensSubsystem = new HuskyLensSubsystem(hardwareMap, telemetry, allianceColor);
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
        telemetry.addData("Alliance Color", getFormattedAllianceColor(allianceColor));
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            AutoMode autoMode = (AutoMode) this;
            telemetry.addData("Starting Field Side", autoMode.startingFieldSide);
            telemetry.addData("Auto Strategy", autoMode.autoStrategyType);
        }
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
        if (getOpModeType() == AUTO) {
            //Store the end pose and alliance color for use in teleop
            AutonomousStorage.endPose = driveSubsystem.getCurrentPose();
            AutonomousStorage.allianceColor = allianceColor;

            //teardown subsystems that are only used in autonomous
            lidarSubsystem.teardown();
//            huskyLensSubsystem.teardown();
        }
        //teardown subsystems that are used in both autonomous and teleop
        intakeSubsystem.teardown();
        limelightSubsystem.teardown();
        turretSubsystem.teardown();
        launchSubsystem.teardown();
        driveSubsystem.teardown(getOpModeType());
        super.reset();
    }

    private Constants.AllianceColor promptForAllianceColor() {
        return promptForSelection(
                Constants.AllianceColor.values(),
                "Select Alliance Color",
                "Current Alliance Color",
                this::getFormattedAllianceColor);
    }

    private Constants.FieldSide promptForStartingFieldSide() {
        return promptForSelection(
                Constants.FieldSide.values(),
                "Select Starting Field Side",
                "Current Starting Field Side",
                value -> value);
    }

    private AutoStrategyType promptForAutoStrategy() {
        return promptForSelection(
                AutoStrategyType.values(),
                "Select Auto Strategy",
                "Current Strategy",
                value -> value);
    }

    private <T extends Enum<T>> T promptForSelection(
            T[] options,
            String promptText,
            String currentLabel,
            java.util.function.Function<T, Object> displayValue) {
        T currentSelection = options[0];
        while (true) {
            telemetry.addData(promptText, "Press DPAD_UP/DPAD_DOWN to select, X to confirm");
            telemetry.addData(currentLabel, displayValue.apply(currentSelection));
            telemetry.update();
            if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                currentSelection = options[(currentSelection.ordinal() + 1) % options.length];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
                currentSelection = options[(currentSelection.ordinal() - 1 + options.length) % options.length];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.X)) {
                return currentSelection;
            }
        }
    }

    private String getFormattedAllianceColor(Constants.AllianceColor allianceColor) {
        return "<font color='" + allianceColor + "'>" + allianceColor + "</font>";
    }

    public Constants.OpModeType getOpModeType() {
        if (this instanceof AutoMode) {
            return Constants.OpModeType.AUTO;
        }
        return Constants.OpModeType.TELEOP;
    }

    public Pose getStartingPose() {
        if (getOpModeType() == Constants.OpModeType.AUTO) {
            AutoMode autoMode = (AutoMode) this;
            return autoMode.startingFieldSide == Constants.FieldSide.AUDIENCE_SIDE
                    ? AUDIENCE_SIDE_START_POSE.getPose(allianceColor)
                    : SCORE_SIDE_START_POSE.getPose(allianceColor);
        } else {
            return AutonomousStorage.endPose == null ? DEFAULT_POSE : AutonomousStorage.endPose;
        }
    }
}
