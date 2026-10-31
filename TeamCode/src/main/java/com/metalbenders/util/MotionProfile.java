package com.metalbenders.util;

/**
 * 2026-27 FTC Season BIOBUZZ
 * <br/>
 * Simple MotionProfile class to handle trapezoidal motion profiling.
 * Used to calculate turret accel/decel
 *
 * @author Team 14739 Metal Benders
 * @version 0.1
 * @since 2026-09-20
 */

public class MotionProfile {
    private double maxPower;
    private double acceleration;
    private double deceleration;
    private double targetPower;
    private double currentPower;

    public MotionProfile(double maxPower, double acceleration, double deceleration) {
        this.maxPower = maxPower;
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.targetPower = 0.0;
        this.currentPower = 0.0;
    }

    /**
     * Updates the motion profile and returns the profiled power.
     *
     * @param desiredPower The target motor power
     * @return The profiled motor power
     */
    public double update(double desiredPower) {
        targetPower = desiredPower;

        // Check if direction change is needed
        boolean directionChange = (desiredPower > 0 && currentPower < 0) || (desiredPower < 0 && currentPower > 0);

        if (directionChange) {
            // Decelerate to zero before changing direction
            if (Math.abs(currentPower) > 0.01) {
                currentPower -= Math.signum(currentPower) * deceleration;
                return currentPower;
            } else {
                // Once stopped, reset current power and start accelerating in the new direction
                currentPower = 0.0;
            }
        }

        if (Math.abs(targetPower - currentPower) < 0.05) {
            return currentPower;
        }

        if (targetPower > currentPower) {
            // Accelerate towards positive power
            currentPower += acceleration;
            if (currentPower > maxPower) {
                // Prevent exceeding maxPower
                currentPower = maxPower;
            }
        } else if (targetPower < currentPower) {
            // Decelerate towards negative power
            currentPower -= deceleration;
            if (currentPower < -maxPower) {
                // Prevent exceeding maxPower
                currentPower = -maxPower;
            }
        }

        // Ensure the current power does not overshoot the target power
        if ((targetPower > 0 && currentPower > targetPower) || (targetPower < 0 && currentPower < targetPower)) {
            currentPower = targetPower;
        }

        return currentPower;
    }

}
