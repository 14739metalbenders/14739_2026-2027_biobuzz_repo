package com.metalbenders.util;

import com.metalbenders.constants.VelocityLookupTable;
import com.pedropathing.math.Twist;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void timeOfFlightScalesWithTargetDistanceWithoutHeightRejection() {
        double horizontalSpeed = Math.PI * 0.072 * (2000.0 / 60) * 0.6
                * Math.cos(Math.toRadians(75));

        assertEquals(1.08 / horizontalSpeed,
                TurretController.calculateTimeOfFlight(2000, 75, 1.08), EPSILON);
        assertEquals(2.0 / horizontalSpeed,
                TurretController.calculateTimeOfFlight(2000, 75, 2.0), EPSILON);
    }

    @Ignore("Shot lead is currently disabled in the turret controller.")
    @Test
    public void calibratedShotsKeepLeadInBothStrafeDirections() {
        double[] distancesInches = {21.1, 26.5, 32.0, 40.8, 45.5, 49.0, 52.0, 56.0, 60.0, 64.0};
        for (double inches : distancesInches) {
            double distance = inches * 0.0254;
            double rpm = VelocityLookupTable.getTargetVelocity(inches) * 60 / 28;
            double expectedTime = distance / (Math.PI * 0.072 * (rpm / 60) * 0.6
                    * Math.cos(Math.toRadians(75)));
            for (double strafe : new double[]{-20, 20}) {
                TurretController.TurretRotateCalc result = TurretController.calculateTurretLead(
                        new Twist(0, strafe, 0), 0, 0, rpm, distance, 75, true);

                assertEquals(expectedTime, result.getTimeOfFlight(), EPSILON);
                assertEquals(Math.toDegrees(Math.atan2(-strafe * 0.0254 * expectedTime, distance)),
                        result.getShotLeadAngle(), EPSILON);
                assertTrue(result.getShotLeadAngle() * strafe < 0);
            }
        }
    }

    @Test
    public void invalidFlightInputsRemainUnavailable() {
        double[][] inputs = {
                {0, 75, 1}, {-1, 75, 1}, {Double.NaN, 75, 1},
                {Double.POSITIVE_INFINITY, 75, 1},
                {2000, 75, 0}, {2000, 75, -1}, {2000, 75, Double.NaN},
                {2000, 75, Double.POSITIVE_INFINITY},
                {2000, -1, 1}, {2000, 90, 1}, {2000, 180, 1},
                {2000, Double.NaN, 1}, {2000, Double.POSITIVE_INFINITY, 1}
        };
        for (double[] input : inputs) {
            assertEquals(0, TurretController.calculateTimeOfFlight(input[0], input[1], input[2]), 0);
        }
    }

    @Test
    public void missingTargetDisablesLeadAndTurretPower() {
        TurretController.TurretRotateCalc result = TurretController.calculateTurretLead(
                new Twist(0, 20, 0), 0, 0, 2400, 1, 75, false);

        assertEquals(0, result.getTimeOfFlight(), 0);
        assertEquals(0, result.getShotLeadAngle(), 0);
        assertEquals(0, result.getTurretPower(), 0);
    }
}
