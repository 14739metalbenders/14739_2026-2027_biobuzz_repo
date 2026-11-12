package com.metalbenders.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.constants.Constants;
import com.metalbenders.util.ConfigFileUtil;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@TeleOp(name = "Config", group = "Config")
public class Config extends LinearOpMode {

    private static final String[] CONFIG_LABELS = {
            "Alliance Color",
            "Starting on Active Side",
            "Waypoint Series Type",
            "Auto Strategy",
            "Save configuration"
    };

    protected GamepadEx driverGamepad;

    private static int wrapIndex(int index, int size) {
        return (index + size) % size;
    }

    private static <T extends Enum<T>> T shiftEnum(T[] values, T current, int direction) {
        return values[wrapIndex(current.ordinal() + direction, values.length)];
    }

    @Override
    public void runOpMode() {
        driverGamepad = new GamepadEx(gamepad1);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);

        telemetry.addLine("Press START to begin configuration");
        telemetry.update();
        waitForStart();

        ConfigFileUtil.ConfigFile configFile = ConfigFileUtil.readConfigFile(telemetry);
        promptForConfiguration(configFile);

        if (isStopRequested()) {
            return;
        }

        telemetry.clearAll();
        try {
            ConfigFileUtil.writeConfigFile(configFile);
            telemetry.addLine("Configuration saved successfully!");
            telemetry.addLine("-----------Saved Configurations------------");
            telemetry.addData("Alliance Color", getFormattedAllianceColor(configFile.getAllianceColor()));
            telemetry.addData("Starting on Active Side", configFile.isStartingOnActiveSide());
            telemetry.addData("Waypoint Series Type", configFile.getWaypointSeriesType());
            telemetry.addData("Auto Strategy", configFile.getAutoStrategy());
        } catch (IOException e) {
            telemetry.addData("Error saving configuration", e.getMessage());
        }
        telemetry.update();
        sleep(5000);
    }

    private void promptForConfiguration(ConfigFileUtil.ConfigFile configFile) {
        int selected = 0;

        while (!isStopRequested()) {
            driverGamepad.readButtons();
            boolean up = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP);
            boolean down = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN);
            boolean left = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_LEFT);
            boolean right = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT);
            boolean a = driverGamepad.wasJustPressed(GamepadKeys.Button.A);

            renderConfigurationMenu(configFile, selected);

            if (up) {
                selected = wrapIndex(selected - 1, CONFIG_LABELS.length);
            } else if (down) {
                selected = wrapIndex(selected + 1, CONFIG_LABELS.length);
            } else if (left || right) {
                int direction = left ? -1 : 1;
                applyHorizontalConfigChange(configFile, selected, direction);
            } else if (a) {
                if (selected == CONFIG_LABELS.length - 1) {
                    return;
                } else if (selected == 3) {
                    configFile.setAutoStrategy(promptForAutoStrategy(configFile.getAutoStrategy()));
                }
            }

            telemetry.update();
            sleep(20);
        }
    }

    private void renderConfigurationMenu(ConfigFileUtil.ConfigFile configFile, int selected) {
        telemetry.addLine("DPAD_UP/DPAD_DOWN to select, DPAD_LEFT/DPAD_RIGHT to change");
        telemetry.addLine("A to edit Auto Strategy or Save configuration");
        telemetry.addLine("Changes are not saved until you select Save configuration");
        telemetry.addLine("-----------Current Configuration-----------");

        Object[] values = {
                getFormattedAllianceColor(configFile.getAllianceColor()),
                configFile.isStartingOnActiveSide() ? "Active Side" : "Inactive Side",
                configFile.getWaypointSeriesType(),
                configFile.getAutoStrategy()
        };

        for (int i = 0; i < CONFIG_LABELS.length; i++) {
            String label = (selected == i ? "> " : "  ") + CONFIG_LABELS[i];
            if (i < values.length) {
                telemetry.addData(label, values[i]);
            } else {
                telemetry.addLine(label);
            }
        }
    }

    private void applyHorizontalConfigChange(
            ConfigFileUtil.ConfigFile configFile,
            int selected,
            int direction) {

        switch (selected) {
            case 0:
                configFile.setAllianceColor(shiftEnum(
                        Constants.AllianceColor.values(),
                        configFile.getAllianceColor(),
                        direction));
                break;
            case 1:
                configFile.setStartingOnActiveSide(!configFile.isStartingOnActiveSide());
                break;
            case 2:
                configFile.setWaypointSeriesType(shiftEnum(
                        Constants.WaypointSeriesType.values(),
                        configFile.getWaypointSeriesType(),
                        direction));
                break;
            default:
                // No horizontal change for other entries.
                break;
        }
    }

    protected List<Constants.AutoChallengeType> promptForAutoStrategy(
            List<Constants.AutoChallengeType> currentSelection) {

        List<Constants.AutoChallengeType> challenges =
                new ArrayList<>(new LinkedHashSet<>(currentSelection));
        enforceChallengeRequirements(challenges);

        int selected = 0;
        while (!isStopRequested()) {
            driverGamepad.readButtons();
            boolean up = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP);
            boolean down = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN);
            boolean left = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_LEFT);
            boolean right = driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT);
            boolean a = driverGamepad.wasJustPressed(GamepadKeys.Button.A);
            boolean b = driverGamepad.wasJustPressed(GamepadKeys.Button.B);
            boolean x = driverGamepad.wasJustPressed(GamepadKeys.Button.X);
            boolean y = driverGamepad.wasJustPressed(GamepadKeys.Button.Y);

            renderAutoStrategyMenu(challenges, selected);

            if (b) {
                return currentSelection;
            } else if (y) {
                return challenges;
            } else if (a) {
                selected = handleAddChallenge(challenges, selected);
            } else if (!challenges.isEmpty()) {
                if (up) {
                    selected = wrapIndex(selected - 1, challenges.size());
                } else if (down) {
                    selected = wrapIndex(selected + 1, challenges.size());
                } else if (x) {
                    selected = handleRemoveChallenge(challenges, selected);
                } else if (left) {
                    selected = moveChallengeEarlier(challenges, selected);
                } else if (right) {
                    selected = moveChallengeLater(challenges, selected);
                }
            }

            sleep(20);
        }

        return currentSelection;
    }

    private void renderAutoStrategyMenu(List<Constants.AutoChallengeType> challenges, int selected) {
        telemetry.addLine("Edit Auto Strategy (execution order)");
        telemetry.addLine("DPAD_UP/DOWN: select, DPAD_LEFT/RIGHT: move earlier/later");
        telemetry.addLine("A: add, X: remove, Y: confirm, B: cancel");

        if (challenges.contains(Constants.AutoChallengeType.PRELOAD)) {
            telemetry.addLine("PRELOAD must remain first; other challenges cannot move ahead of it.");
        }
        if (challenges.contains(Constants.AutoChallengeType.PARK)) {
            telemetry.addLine("PARK must remain last; other challenges cannot move after it.");
        }
        if (requiresPreload(challenges)) {
            telemetry.addLine("PRELOAD is required by garden/flower challenges and cannot be removed.");
        }
        if (challenges.isEmpty()) {
            telemetry.addLine("No challenges selected. Press A to add.");
        }

        for (int i = 0; i < challenges.size(); i++) {
            telemetry.addData((selected == i ? "> " : "  ") + (i + 1), challenges.get(i));
        }

        if (challenges.size() == Constants.AutoChallengeType.values().length) {
            telemetry.addLine("All challenge types are already included.");
        }

        telemetry.update();
    }

    private int handleAddChallenge(List<Constants.AutoChallengeType> challenges, int selected) {
        List<Constants.AutoChallengeType> available =
                new ArrayList<>(Arrays.asList(Constants.AutoChallengeType.values()));
        available.removeAll(challenges);

        if (available.isEmpty()) {
            return selected;
        }

        Constants.AutoChallengeType added = promptForSelection(
                available.toArray(new Constants.AutoChallengeType[0]),
                "Add Auto Challenge",
                "Challenge",
                null);

        if (added != null && !isStopRequested()) {
            challenges.add(added);
            enforceChallengeRequirements(challenges);
            return challenges.indexOf(added);
        }

        return selected;
    }

    private int handleRemoveChallenge(List<Constants.AutoChallengeType> challenges, int selected) {
        Constants.AutoChallengeType current = challenges.get(selected);
        if (current != Constants.AutoChallengeType.PRELOAD || !requiresPreload(challenges)) {
            challenges.remove(selected);
            return Math.max(0, Math.min(selected, challenges.size() - 1));
        }
        return selected;
    }

    private int moveChallengeEarlier(List<Constants.AutoChallengeType> challenges, int selected) {
        if (selected > 0
                && challenges.get(selected) != Constants.AutoChallengeType.PARK
                && challenges.get(selected - 1) != Constants.AutoChallengeType.PRELOAD) {
            Collections.swap(challenges, selected, selected - 1);
            return selected - 1;
        }
        return selected;
    }

    private int moveChallengeLater(List<Constants.AutoChallengeType> challenges, int selected) {
        if (selected < challenges.size() - 1
                && challenges.get(selected + 1) != Constants.AutoChallengeType.PARK
                && challenges.get(selected) != Constants.AutoChallengeType.PRELOAD) {
            Collections.swap(challenges, selected, selected + 1);
            return selected + 1;
        }
        return selected;
    }

    private boolean requiresPreload(List<Constants.AutoChallengeType> challenges) {
        return challenges.stream().anyMatch(challenge ->
                challenge != Constants.AutoChallengeType.PARK
                        && challenge != Constants.AutoChallengeType.PRELOAD);
    }

    private void enforceChallengeRequirements(List<Constants.AutoChallengeType> challenges) {
        if (requiresPreload(challenges) && !challenges.contains(Constants.AutoChallengeType.PRELOAD)) {
            challenges.add(0, Constants.AutoChallengeType.PRELOAD);
        }

        int preloadIndex = challenges.indexOf(Constants.AutoChallengeType.PRELOAD);
        if (preloadIndex > 0) {
            challenges.remove(preloadIndex);
            challenges.add(0, Constants.AutoChallengeType.PRELOAD);
        }

        int parkIndex = challenges.indexOf(Constants.AutoChallengeType.PARK);
        if (parkIndex >= 0 && parkIndex < challenges.size() - 1) {
            challenges.remove(parkIndex);
            challenges.add(Constants.AutoChallengeType.PARK);
        }
    }

    private <T> T promptForSelection(
            T[] options,
            String promptText,
            String currentLabel,
            T initialSelection) {

        T currentSelection = initialSelection != null ? initialSelection : options[0];
        List<T> selections = Arrays.asList(options);

        while (!isStopRequested()) {
            driverGamepad.readButtons();
            telemetry.addData(promptText, "Press DPAD_UP/DPAD_DOWN to select, A to confirm, B to cancel");
            telemetry.addData(currentLabel, currentSelection);
            telemetry.update();

            if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                currentSelection = options[wrapIndex(selections.indexOf(currentSelection) - 1, options.length)];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
                currentSelection = options[wrapIndex(selections.indexOf(currentSelection) + 1, options.length)];
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.A)) {
                return currentSelection;
            } else if (driverGamepad.wasJustPressed(GamepadKeys.Button.B)) {
                return initialSelection;
            }

            sleep(20);
        }

        return currentSelection;
    }

    private String getFormattedAllianceColor(Constants.AllianceColor allianceColor) {
        return "<font color='" + allianceColor.name() + "'>" + allianceColor.name() + "</font>";
    }
}