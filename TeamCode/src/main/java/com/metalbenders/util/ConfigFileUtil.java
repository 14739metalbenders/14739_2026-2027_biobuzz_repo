package com.metalbenders.util;

import com.metalbenders.strategies.AutoStrategyType;
import com.metalbenders.constants.Constants;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.*;
import java.util.Properties;

public class ConfigFileUtil {

    public static final String ALLIANCE_COLOR_KEY = "allianceColor";
    public static final String STARTING_FIELD_SIDE_KEY = "startingFieldSide";
    public static final String AUTO_STRATEGY_KEY = "autoStrategy";
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
        config.startingFieldSide = parseEnum(properties, STARTING_FIELD_SIDE_KEY, config.startingFieldSide);
        config.autoStrategyType = parseEnum(properties, AUTO_STRATEGY_KEY, config.autoStrategyType);
        return config;
    }

    public static void writeConfigFile(ConfigFile configFile) throws IOException {
        Properties propertiesToStore = new Properties();
        propertiesToStore.setProperty(ALLIANCE_COLOR_KEY, configFile.getAllianceColor().name());
        propertiesToStore.setProperty(STARTING_FIELD_SIDE_KEY, configFile.getStartingFieldSide().name());
        propertiesToStore.setProperty(AUTO_STRATEGY_KEY, configFile.getAutoStrategyType().name());
        File file = AppUtil.getInstance().getSettingsFile(CONFIG_FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            propertiesToStore.store(outputStream, null);
        }
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
        private Constants.FieldSide startingFieldSide = Constants.FieldSide.AUDIENCE_SIDE;
        private AutoStrategyType autoStrategyType = AutoStrategyType.PRELOAD_AND_PARK;

        public Constants.AllianceColor getAllianceColor() {
            return allianceColor;
        }

        public void setAllianceColor(Constants.AllianceColor allianceColor) {
            this.allianceColor = allianceColor;
        }

        public Constants.FieldSide getStartingFieldSide() {
            return startingFieldSide;
        }

        public void setStartingFieldSide(Constants.FieldSide startingFieldSide) {
            this.startingFieldSide = startingFieldSide;
        }

        public AutoStrategyType getAutoStrategyType() {
            return autoStrategyType;
        }

        public void setAutoStrategyType(AutoStrategyType autoStrategyType) {
            this.autoStrategyType = autoStrategyType;
        }
    }
}
