package com.metalbenders.control.subsystems;

import com.pedropathing.math.Pose;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DriveSubsystemTest {
    @Test
    public void headingErrorWrapsAcrossTheBoundary() {
        Pose odometry = new Pose(0, 0, Math.toRadians(179));
        Pose vision = new Pose(0, 0, Math.toRadians(-179));

        double error = DriveSubsystem.visionHeadingErrorDegrees(odometry, vision);
        assertEquals(2.0, error, 1e-8);
        assertTrue(DriveSubsystem.isVisionHeadingConsistent(error));
    }

    @Test
    public void rejectsHeadingErrorsBeyondFifteenDegreesInEitherDirection() {
        assertTrue(DriveSubsystem.isVisionHeadingConsistent(15.0));
        assertTrue(DriveSubsystem.isVisionHeadingConsistent(-15.0));
        assertFalse(DriveSubsystem.isVisionHeadingConsistent(15.1));
        assertFalse(DriveSubsystem.isVisionHeadingConsistent(-15.1));
    }
}
