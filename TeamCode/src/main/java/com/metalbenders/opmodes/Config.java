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

    @Override
    public void runOpMode() {
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Press START to begin configuration");
        telemetry.update();
        waitForStart();
        ConfigFileUtil.ConfigFile configFile = ConfigFileUtil.readConfigFile(telemetry);

        while (!isStopRequested()) {
            promptForConfiguration(configFile);
            if (isStopRequested()) {
                return;
            }

            try {
                ConfigFileUtil.writeConfigFile(configFile);
                telemetry.addLine("Configuration saved successfully!");
                telemetry.addLine("-----------Saved Configurations------------");
                telemetry.addData("Alliance Color", configFile.getAllianceColor());
                telemetry.addData("Starting Field Side", configFile.getStartingFieldSide());
                telemetry.addData("Auto Strategy", configFile.getAutoStrategyType());
            } catch (IOException e) {
                telemetry.addData("Error saving configuration", e.getMessage());
            }
            telemetry.update();
            sleep(2000);
        }
    }

    private void promptForConfiguration(ConfigFileUtil.ConfigFile configFile) {
        List<Runnable> editors = Arrays.asList(
                () -> configFile.setAllianceColor(promptForAllianceColor(configFile.getAllianceColor())),
                () -> configFile.setStartingFieldSide(promptForStartingFieldSide(configFile.getStartingFieldSide())),
                () -> configFile.setAutoStrategyType(promptForAutoStrategy(configFile.getAutoStrategyType())));
        String[] labels = {"Alliance Color", "Starting Field Side", "Auto Strategy", "Save configuration"};
        int selected = 0;
        while (!isStopRequested()) {
            driverGamepad.readButtons();
            telemetry.addLine("DPAD_UP/DPAD_DOWN to select, X to edit or save");
            telemetry.addLine("Changes are not saved until you select Save configuration");
            telemetry.addLine("-----------Current Configuration-----------");
            Object[] values = {
                    configFile.getAllianceColor(),
                    configFile.getStartingFieldSide(),
                    configFile.getAutoStrategyType()
            };
            for (int i = 0; i < labels.length; i++) {
                String label = (selected == i ? "> " : "  ") + labels[i];
                if (i < values.length) {
                    telemetry.addData(label, values[i]);
                } else {
                    telemetry.addLine(label);
                }
            }
            telemetry.update();
            if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                selected = (selected - 1 + labels.length) % labels.length;
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
                selected = (selected + 1) % labels.length;
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.X)) {
                if (selected == editors.size()) {
                    return;
                }
                editors.get(selected).run();
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
            telemetry.addData(promptText, "Press DPAD_UP/DPAD_DOWN to select, X to confirm, B to cancel");
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
                return initialSelection;
            }
            sleep(20);
        }
        return currentSelection;
    }
}
