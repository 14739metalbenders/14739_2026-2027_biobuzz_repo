package com.metalbenders.control.subsystems;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LidarSubsystemTest {
    @Test
    public void obstacleDistanceIncludes127MillimetersButIgnoresInvalidReadings() {
        assertTrue(LidarSubsystem.isObstacleWithinDistance(new double[]{127.0}, 127.0));
        assertTrue(LidarSubsystem.isObstacleWithinDistance(new double[]{0, -1, 126.9}, 127.0));
        assertFalse(LidarSubsystem.isObstacleWithinDistance(new double[]{0, -1, 127.1}, 127.0));
    }
}
