package com.metalbenders.util;

import com.metalbenders.constants.Constants;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigFileUtil {

    public static final String ALLIANCE_COLOR_KEY = "allianceColor";
    public static final String STARTING_ON_ACTIVE_SIDE_KEY = "startingOnActiveSide";
    public static final String AUTO_STRATEGY_KEY = "autoStrategy";
    private static final String WAYPOINTS_KEY = "waypoints";
    private static final String CONFIG_FILE_NAME = "config.properties";

    public static ConfigFile readConfigFile(Telemetry telemetry) {
        File file = AppUtil.getInstance().getSettingsFile(CONFIG_FILE_NAME);
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            telemetry.addData("Error reading config file, returning defaults", e);
        }
        ConfigFile config = new ConfigFile();
        config.allianceColor = parseEnum(properties, ALLIANCE_COLOR_KEY, config.allianceColor);
        String startingOnActiveSide = properties.getProperty(STARTING_ON_ACTIVE_SIDE_KEY);
        if (startingOnActiveSide != null) {
            if ("true".equalsIgnoreCase(startingOnActiveSide.trim())
                    || "false".equalsIgnoreCase(startingOnActiveSide.trim())) {
                config.startingOnActiveSide = Boolean.parseBoolean(startingOnActiveSide.trim());
            } else {
                telemetry.addData("Invalid startingOnActiveSide, returning default", startingOnActiveSide);
            }
        }
        String autoStrategy = properties.getProperty(AUTO_STRATEGY_KEY);
        if (autoStrategy != null) {
            try {
                config.autoStrategy = parseAutoStrategy(autoStrategy);
            } catch (IllegalArgumentException e) {
                telemetry.addData("Invalid autoStrategy, returning defaults", autoStrategy);
            }
        }
        config.waypointSeriesType = parseEnum(properties, WAYPOINTS_KEY, config.waypointSeriesType);
        return config;
    }

    public static void writeConfigFile(ConfigFile configFile) throws IOException {
        Properties propertiesToStore = new Properties();
        propertiesToStore.setProperty(ALLIANCE_COLOR_KEY, configFile.getAllianceColor().name());
        propertiesToStore.setProperty(STARTING_ON_ACTIVE_SIDE_KEY,
                Boolean.toString(configFile.isStartingOnActiveSide()));
        propertiesToStore.setProperty(AUTO_STRATEGY_KEY, configFile.getAutoStrategy().toString());
        propertiesToStore.setProperty(WAYPOINTS_KEY, configFile.getWaypointSeriesType().name());
        File file = AppUtil.getInstance().getSettingsFile(CONFIG_FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            propertiesToStore.store(outputStream, null);
        }
    }

    private static List<Constants.AutoChallengeType> parseAutoStrategy(String value) {
        String trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Expected a list of auto challenges");
        }
        String contents = trimmed.substring(1, trimmed.length() - 1).trim();
        List<Constants.AutoChallengeType> challenges = new ArrayList<>();
        if (!contents.isEmpty()) {
            for (String name : contents.split(",", -1)) {
                Constants.AutoChallengeType challenge =
                        Constants.AutoChallengeType.valueOf(name.trim());
                if (challenges.contains(challenge)) {
                    throw new IllegalArgumentException("Duplicate auto challenge: " + challenge);
                }
                challenges.add(challenge);
            }
        }
        return challenges;
    }

    // Missing or unrecognized values (e.g. a renamed enum constant) fall back to the default
    private static <T extends Enum<T>> T parseEnum(Properties properties, String key, T defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), value.trim());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static class ConfigFile {
        private Constants.AllianceColor allianceColor = Constants.AllianceColor.RED;
        private boolean startingOnActiveSide = true;
        private List<Constants.AutoChallengeType> autoStrategy = Arrays.asList(Constants.AutoChallengeType.values());
        private Constants.WaypointSeriesType waypointSeriesType = Constants.WaypointSeriesType.OUTER;

        public Constants.AllianceColor getAllianceColor() {
            return allianceColor;
        }

        public void setAllianceColor(Constants.AllianceColor allianceColor) {
            this.allianceColor = allianceColor;
        }

        public boolean isStartingOnActiveSide() {
            return startingOnActiveSide;
        }

        public void setStartingOnActiveSide(boolean startingOnActiveSide) {
            this.startingOnActiveSide = startingOnActiveSide;
        }

        public List<Constants.AutoChallengeType> getAutoStrategy() {
            return autoStrategy;
        }

        public void setAutoStrategy(List<Constants.AutoChallengeType> autoStrategy) {
            this.autoStrategy = autoStrategy;
        }

        public Constants.WaypointSeriesType getWaypointSeriesType() {
            return waypointSeriesType;
        }

        public void setWaypointSeriesType(Constants.WaypointSeriesType waypointSeriesType) {
            this.waypointSeriesType = waypointSeriesType;
        }
    }
}
