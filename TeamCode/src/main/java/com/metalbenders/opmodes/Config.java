package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.strategies.AutoStrategyType;
import com.metalbenders.constants.Constants;
import com.metalbenders.util.ConfigFileUtil;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@TeleOp(name = "Config", group = "Config")
public class Config extends LinearOpMode {

    protected GamepadEx driverGamepad;
    private boolean backRequested;

    @Override
    public void runOpMode() {
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Press START to begin configuration");
        telemetry.update();
        waitForStart();
        ConfigFileUtil.ConfigFile configFile = ConfigFileUtil.readConfigFile(telemetry);

        List<Runnable> steps = Arrays.asList(
                () -> configFile.setAllianceColor(promptForAllianceColor(configFile.getAllianceColor())),
                () -> configFile.setStartingFieldSide(promptForStartingFieldSide(configFile.getStartingFieldSide())),
                () -> configFile.setAutoStrategyType(promptForAutoStrategy(configFile.getAutoStrategyType())),
                () -> promptForConfirmation(configFile));
        int step = 0;
        while (step < steps.size() && !isStopRequested()) {
            backRequested = false;
            steps.get(step).run();
            step = backRequested ? Math.max(0, step - 1) : step + 1;
        }
        if (isStopRequested()) {
            return;
        }

        // Only reached once X was pressed on the confirmation step
        try {
            ConfigFileUtil.writeConfigFile(configFile);
            telemetry.addLine("Configuration saved Successfully!");
        } catch (IOException e) {
            telemetry.addData("Error saving configuration", e.getMessage());
        }
        telemetry.update();
        sleep(2000);
    }

    private void promptForConfirmation(ConfigFileUtil.ConfigFile configFile) {
        while (!isStopRequested()) {
            driverGamepad.readButtons();
            telemetry.addLine("Confirm configuration: X to save and exit, B to go back");
            telemetry.addData("Alliance Color", configFile.getAllianceColor());
            telemetry.addData("Starting Field Side", configFile.getStartingFieldSide());
            telemetry.addData("Auto Strategy", configFile.getAutoStrategyType());
            telemetry.update();
            if (driverGamepad.wasJustPressed(GamepadKeys.Button.X)) {
                return;
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.B)) {
                backRequested = true;
                return;
            }
            sleep(20);
        }
    }

    protected Constants.AllianceColor promptForAllianceColor(Constants.AllianceColor currentSelection) {
        return promptForSelection(
                Constants.AllianceColor.values(),
                "Select Alliance Color",
                "Current Alliance Color",
                currentSelection);
    }

    protected Constants.FieldSide promptForStartingFieldSide(Constants.FieldSide currentSelection) {
        return promptForSelection(
                Constants.FieldSide.values(),
                "Select Starting Field Side",
                "Current Starting Field Side",
                currentSelection);
    }

    protected AutoStrategyType promptForAutoStrategy(AutoStrategyType currentSelection) {
        return promptForSelection(
                AutoStrategyType.values(),
                "Select Auto Strategy",
                "Current Strategy",
                currentSelection,
                AutoStrategyType::getDescription);
    }

    private <T extends Enum<T>> T promptForSelection(
            T[] options,
            String promptText,
            String currentLabel,
            T initialSelection) {
        return promptForSelection(options, promptText, currentLabel, initialSelection, null);
    }

    private <T extends Enum<T>> T promptForSelection(
            T[] options,
            String promptText,
            String currentLabel,
            T initialSelection,
            Function<T, String> describer) {
        T currentSelection = initialSelection != null ? initialSelection : options[0];
        while (!isStopRequested()) {
            driverGamepad.readButtons();
            telemetry.addData(promptText, "Press DPAD_UP/DPAD_DOWN to select, X to confirm, B to go back");
            telemetry.addData(currentLabel, currentSelection);
            if (describer != null) {
                telemetry.addData("Description", describer.apply(currentSelection));
            }
            telemetry.update();
            if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                currentSelection = options[(currentSelection.ordinal() + 1) % options.length];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
                currentSelection = options[(currentSelection.ordinal() - 1 + options.length) % options.length];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.X)) {
                return currentSelection;
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.B)) {
                backRequested = true;
                return currentSelection;
            }
            sleep(20);
        }
        return currentSelection;
    }
}
