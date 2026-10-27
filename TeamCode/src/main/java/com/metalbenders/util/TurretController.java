package com.metalbenders.util;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Config
public class TurretController {

        private static final double GRAVITY = 9.81; // in m/s2

        // PID controller parameters
        public static double AIM_Kp = 0.025;
        public static double AIM_Ki = 0;
        public static double AIM_Kd = 0.03;
        public static double AIM_Ks = 0.02;
        public static double AIM_DEADBAND = 2.0;
        private static double aimIntegral = 0;
        private static double aimLastError = 0;
        private static final ElapsedTime aimTimer = new ElapsedTime();
        private static double previousPower; // Previous power output value for rate limiting
        public static double maxRateChange = 0.15; // Maximum allowed change in output per iteration
        public static double powerLimit = 1.0; // Maximum motor power
        public static double turretAngleLimit = 45; // Maximum angle limit for turret movement
        private static double integral;
        private static double previousError;
        private static final double INCHES_TO_METERS = 0.0254;

    public TurretController(double Kp, double Ki, double Kd, double Ks) {
        this.AIM_Kp = Kp;
        this.AIM_Ki = Ki;
        this.AIM_Kd = Kd;
        this.AIM_Ks = Ks;
    }

    public static TurretRotateCalc calculateTurretLead(Twist twist, double targetBearing, double turretAngle, double ballLaunchVelocity, double ballLaunchAngle) {
        // Convert Twist velocities from inches per second to meters per second
        double vx_mps = twist.vx * INCHES_TO_METERS;
        double vy_mps = twist.vy * INCHES_TO_METERS;
        double turretPower = 0;

        // Calculate time of flight
        double timeOfFlight = calculateTimeOfFlight(ballLaunchVelocity, ballLaunchAngle);

        // Calculate shot lead angle
        double shotLeadAngle = calculateShotLead(vx_mps, vy_mps, twist.omega, timeOfFlight, turretAngle, ballLaunchVelocity);

        if (Math.abs(targetBearing) > 0) {
            // Desired turret angle is the current bearing plus the shot lead angle
            double desiredTurretAngle = targetBearing + shotLeadAngle;

            // Clamp desired turret angle to within -90 and 90 degrees
            desiredTurretAngle = Range.clip((desiredTurretAngle + turretAngle), -turretAngleLimit, turretAngleLimit);

            double turretError = desiredTurretAngle - turretAngle;

            // Calculate PID control for turret motor
            double pidOutput = calculatePID(turretError);

            // Apply rate limiting to the PID output
            double clampedOutput = applyRateLimiting(pidOutput);

            // Clamp PID output to range [-1, 1]
            turretPower = Range.clip(clampedOutput, -powerLimit, powerLimit);
        } else {
            turretPower = 0;
            shotLeadAngle = 0;
            timeOfFlight = 0;
        }

        return new TurretRotateCalc(turretPower, targetBearing, shotLeadAngle, timeOfFlight);
    }

    private static double calculateTimeOfFlight(double ballLaunchVelocity, double ballLaunchAngle) {
        // Using projectile motion equations
        double verticalVelocity = ballLaunchVelocity * Math.sin(Math.toRadians(ballLaunchAngle));
        double heightDifference = 1.261 - 0.300;

        // Quadratic equation to solve for time of flight: h = vt + 0.5at^2
        double a = -0.5 * GRAVITY;
        double b = verticalVelocity;
        double c = heightDifference;

        double discriminant = Math.pow(b, 2) - (4 * a * c);
        if (discriminant < 0) {
            return Double.NaN; // No real solution, error in parameters
        }

        double timeOfFlight1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double timeOfFlight2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        return Math.max(timeOfFlight1, timeOfFlight2); // Choose the positive solution
    }

    private static double calculateShotLead(double vx_mps, double vy_mps, double omega_rad_s, double timeOfFlight, double turretAngle, double ballLaunchVelocity) {
        // Calculate linear velocity component in the direction of the target
        double linearVelocityComponent = vx_mps * Math.cos(Math.toRadians(turretAngle)) +
                vy_mps * Math.sin(Math.toRadians(turretAngle));

        // Convert angular velocity from radians per second to degrees per second
        double omega_deg_s = Math.toDegrees(omega_rad_s);

        // Calculate angular contribution to shot lead angle
        double angularContribution = omega_deg_s * timeOfFlight;

        // Total shot lead angle in degrees
        return Math.toDegrees(linearVelocityComponent / ballLaunchVelocity) + angularContribution;
    }

    private static double calculatePID(double error) {
        double dt = aimTimer.seconds();
        aimTimer.reset();

        // Integral
        aimIntegral += error * dt;

        // Derivative
        double derivative = (error - aimLastError) / dt;
        derivative = Range.clip(derivative, -50, 50); // prevent explosion on large error jumps
        aimLastError = error;

        if (Math.abs(error) < AIM_DEADBAND) {
            aimIntegral = 0;
            return 0;
        }

        double output = AIM_Kp * error
                + AIM_Ki * aimIntegral
                + AIM_Kd * derivative
                + AIM_Ks * Math.signum(error);

        // Clamp for safety
        output = Range.clip(output, -1, 1);

        return output;
    }

    private static double applyRateLimiting(double turretPower) {
        // Calculate the change in output
        double change = turretPower - previousPower;

        // Clamp the change to the maximum allowed rate change
        if (change > maxRateChange) {
            turretPower = previousPower + maxRateChange;
        } else if (change < -maxRateChange) {
            turretPower = previousPower - maxRateChange;
        }

        // Update the previous output
        previousPower = turretPower;

        return turretPower;
    }

    public TurretController(double Kp, double Ki, double Kd, double Ks, double maxRateChange) {
        this.AIM_Kp = Kp;
        this.AIM_Ki = Ki;
        this.AIM_Kd = Kd;
        this.AIM_Ks = Ks;
        this.previousPower = 0.0;
        this.maxRateChange = maxRateChange;
    }

    public static class TurretRotateCalc {
        private double turretPower;
        private double targetBearing;
        private double shotLeadAngle;
        private double timeOfFlight;

        public TurretRotateCalc(double turretPower, double targetBearing, double shotLeadAngle, double timeOfFlight) {
            this.turretPower = turretPower;
            this.targetBearing = targetBearing;
            this.shotLeadAngle = shotLeadAngle;
            this.timeOfFlight = timeOfFlight;
        }

        public double getTurretPower() {
            return turretPower;
        }

        public double getTargetBearing() {
            return targetBearing;
        }

        public double getShotLeadAngle() {
            return shotLeadAngle;
        }

        public double getTimeOfFlight() {
            return timeOfFlight;
        }
    }

}
