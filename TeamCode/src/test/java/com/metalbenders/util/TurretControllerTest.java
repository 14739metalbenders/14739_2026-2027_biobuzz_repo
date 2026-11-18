package com.metalbenders.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TurretControllerTest {
    private static final double EPSILON = 1e-6;

    @Test
    public void shotLeadAccountsForOffAxisTargetBearing() {
        double targetX = 2 * Math.cos(Math.toRadians(45));
        double targetY = 2 * Math.sin(Math.toRadians(45));
        double lead = TurretController.calculateShotLead(1, 0, 0.5, 2, 45);

        assertEquals(Math.toDegrees(Math.atan2(targetY, targetX - 0.5)) - 45, lead, EPSILON);
    }

    @Test
    public void shotLeadCompensatesForMotionFromCenteredBearing() {
        double lead = TurretController.calculateShotLead(0, 1, 0.5, 2, 0);

        assertEquals(Math.toDegrees(Math.atan2(-0.5, 2)), lead, EPSILON);
    }

    @Test
    public void timeOfFlightUsesTargetDistanceAndRejectsMissedHeight() {
        double originalTolerance = TurretController.TARGET_HEIGHT_TOLERANCE_METERS;
        try {
            TurretController.TARGET_HEIGHT_TOLERANCE_METERS = 0.2;
            double timeToTarget = TurretController.calculateTimeOfFlight(2000, 75, 1.08);
            double timeToMissedTarget = TurretController.calculateTimeOfFlight(2000, 75, 2.0);

            assertEquals(1.08 / (Math.PI * 0.072 * (2000.0 / 60) * 0.7 * Math.cos(Math.toRadians(75))),
                    timeToTarget, EPSILON);
            assertEquals(0, timeToMissedTarget, EPSILON);
        } finally {
            TurretController.TARGET_HEIGHT_TOLERANCE_METERS = originalTolerance;
        }
    }
}
