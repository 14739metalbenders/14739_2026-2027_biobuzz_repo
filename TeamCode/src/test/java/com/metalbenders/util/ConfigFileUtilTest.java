package com.metalbenders.util;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigFileUtilTest {
    @Test
    public void crossingDefaultsToAllowedAndRoundTrips() {
        List<String> messages = new ArrayList<>();
        Telemetry telemetry = telemetry(messages);
        ConfigFileUtil.ConfigFile defaults = ConfigFileUtil.fromProperties(new Properties(), telemetry);
        assertTrue(defaults.isFieldCrossingAllowed());
        defaults.setFieldCrossingAllowed(false);
        Properties saved = ConfigFileUtil.toProperties(defaults);
        assertEquals("false", saved.getProperty(ConfigFileUtil.ALLOW_FIELD_CROSSING_KEY));
        assertFalse(ConfigFileUtil.fromProperties(saved, telemetry).isFieldCrossingAllowed());
        defaults.setFieldCrossingAllowed(true);
        assertTrue(ConfigFileUtil.fromProperties(ConfigFileUtil.toProperties(defaults), telemetry)
                .isFieldCrossingAllowed());
    }

    @Test
    public void invalidCrossingSettingReportsErrorAndDefaultsToAllowed() {
        List<String> messages = new ArrayList<>();
        Properties properties = new Properties();
        properties.setProperty(ConfigFileUtil.ALLOW_FIELD_CROSSING_KEY, "invalid");
        assertTrue(ConfigFileUtil.fromProperties(properties, telemetry(messages)).isFieldCrossingAllowed());
        assertTrue(messages.contains("Invalid allowFieldCrossing, returning default"));
    }

    private Telemetry telemetry(List<String> messages) {
        return (Telemetry) Proxy.newProxyInstance(Telemetry.class.getClassLoader(),
                new Class<?>[]{Telemetry.class}, (proxy, method, args) -> {
                    if (method.getName().equals("addData")) {
                        messages.add((String) args[0]);
                    }
                    return null;
                });
    }
}
