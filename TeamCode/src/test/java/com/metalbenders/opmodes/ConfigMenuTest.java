package com.metalbenders.opmodes;

import com.metalbenders.constants.Constants;
import com.metalbenders.util.ConfigFileUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigMenuTest {
    @Test
    public void crossingToggleDoesNotChangeOtherMenuEntries() {
        ConfigFileUtil.ConfigFile config = new ConfigFileUtil.ConfigFile();
        Config.applyHorizontalConfigChange(config, 4, 1);
        assertFalse(config.isFieldCrossingAllowed());
        assertEquals(Constants.AllianceColor.RED, config.getAllianceColor());
        assertEquals(Constants.WaypointSeriesType.OUTER, config.getWaypointSeriesType());
        Config.applyHorizontalConfigChange(config, 4, -1);
        assertTrue(config.isFieldCrossingAllowed());
        Config.applyHorizontalConfigChange(config, 3, 1);
        Config.applyHorizontalConfigChange(config, 5, 1);
        assertTrue(config.isFieldCrossingAllowed());
    }
}
