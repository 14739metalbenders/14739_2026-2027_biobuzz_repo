package com.metalbenders.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShotReadinessIndicatorTest {
    @Test
    public void requiresAValidTargetAndBothMeasurementsWithinTolerance() {
        ShotReadinessIndicator indicator = new ShotReadinessIndicator();
        assertTrue(indicator.update(0, false, true, 980, 1000, 1.49, 1.5));
        assertTrue(indicator.update(0, false, true, 1020, 1000, -1.49, 1.5));
        assertFalse(indicator.update(0, false, true, 1020.01, 1000, 0, 1.5));
        assertFalse(indicator.update(0, false, true, 1000, 1000, 1.5, 1.5));
        assertFalse(indicator.update(0, false, false, 1000, 1000, 0, 1.5));
        assertFalse(indicator.update(0, false, true, 0, 0, 0, 1.5));
        assertFalse(indicator.update(0, false, true, Double.NaN, 1000, 0, 1.5));
        assertFalse(indicator.update(0, false, true, 1000, 1000, Double.NaN, 1.5));
    }

    @Test
    public void shootingSuppressesReadinessUntilRecoveryAfterGateCloses() {
        ShotReadinessIndicator indicator = new ShotReadinessIndicator();
        assertTrue(indicator.update(0, false, true, 1000, 1000, 0, 1.5));
        assertFalse(indicator.update(1, true, true, 900, 1000, 0, 1.5));
        assertFalse(indicator.update(1, false, true, 1000, 1000, 0, 1.5));
        assertFalse(indicator.update(1, false, true, 1000, 1000, 2, 1.5));
        assertTrue(indicator.update(1, false, true, 1000, 1000, 0, 1.5));
    }

    @Test
    public void detectsShotEvenIfGateOpenedAndClosedBetweenUpdates() {
        ShotReadinessIndicator indicator = new ShotReadinessIndicator();
        assertTrue(indicator.update(0, false, true, 1000, 1000, 0, 1.5));
        assertFalse(indicator.update(1, false, true, 1000, 1000, 0, 1.5));
        assertFalse(indicator.update(1, false, true, 979, 1000, 0, 1.5));
        assertTrue(indicator.update(1, false, true, 980, 1000, 0, 1.5));
    }
}
