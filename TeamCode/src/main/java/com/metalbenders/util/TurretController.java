package com.metalbenders.util;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Config
public class TurretController {

        private static final double GRAVITY = 9.81; // in m/s2

        // PID controller parameters
        public static double AIM_Kp = 0.022;
        public static double AIM_Ki = 0;
        public static double AIM_Kd = 0.002;
        public static double AIM_Ks = 0.002;
        public static double AIM_DEADBAND = 2.0;
        public static double maxTurretPower = 0.9;
        public static double maxTurretAccel = 0.15;
        public static double maxTurretDecel = 0.3;
        private static final MotionProfile profiledPower = new MotionProfile(maxTurretPower, maxTurretAccel, maxTurretDecel);
        private static double aimIntegral = 0;
        private static double aimLastError = 0;
        private static final ElapsedTime aimTimer = new ElapsedTime();
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

    /**
     * Calculates launch velocity for a 72mm flywheel.
     *
     * @param currentRPM Flywheel RPM
     * @return Launch velocity in m/s
     */
    private static double calculateLaunchVelocity(double currentRPM) {
        // PI * Flywheel Diameter(m) * RPS * Efficiency Factor
        double efficencyFactor = 0.7;
        return Math.PI * 0.072 * (currentRPM / 60) * efficencyFactor;
    }


    /**
     * Calculates time of flight for a projectile
     *
     * @param currentRPM Flywheel RPM
     * @param ballLaunchAngle Launch angle of projectile
     * @return Time of flight for projectile
     */
    private static double calculateTimeOfFlight(double currentRPM, double ballLaunchAngle) {
        // Using projectile motion equations
        double verticalVelocity = calculateLaunchVelocity(currentRPM) * Math.sin(Math.toRadians(ballLaunchAngle));
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

    /**
     * Calculates the target lead angle based on robot's current movement and time of flight.
     *
     * @param xVector X vector of the robot's motion in inches per second (Turret Reference Frame)
     * @param yVector Y vector of the robot's motion in inches per second (Turret Reference Frame)
     * @param angularMoment Angular moment of the robot in degrees per second
     * @param timeOfFlight Ball time of flight
     * @return Shot lead in degrees
     */
    private static double calculateShotLead(double xVector, double yVector, double angularMoment, double timeOfFlight) {
        // Calculate components of target lead angle
        double xComponent = xVector * timeOfFlight;
        double yComponent = yVector * timeOfFlight;
        double angularComponent = angularMoment * timeOfFlight;

        // Convert Cartesian coordinates to polar coordinates (bearing)
        return Math.toDegrees(Math.atan2(yComponent, xComponent)) + angularComponent;
    }

    /**
     * Calculates PID for turret
     *
     * @param error distance in degrees of turret from target
     * @return turretPower after PID adjustment
     */
    private static double calculatePID(double error) {
        double dt = aimTimer.seconds();

        // Protect against division by zero if the loop ticks incredibly fast
        if (dt <= 0) {
            dt = 0.001;
        }

        // 1. Check deadband FIRST to skip unnecessary math
        if (Math.abs(error) < AIM_DEADBAND) {
            aimIntegral = 0;
            aimLastError = error;
            aimTimer.reset(); // Still reset timer so dt doesn't explode for the next cycle
            return 0;
        }

        // 2. Integral calculation with windup protection (clamping)
        aimIntegral += error * dt;
        aimIntegral = Range.clip(aimIntegral, -0.5, 0.5); // Example bounds

        // 3. Derivative calculation
        double derivative = (error - aimLastError) / dt;
        derivative = Range.clip(derivative, -50, 50);
        aimLastError = error;

        // 4. Reset timer at the end of the math block
        aimTimer.reset();

        // 5. Calculate total output
        double output = (AIM_Kp * error)
                + (AIM_Ki * aimIntegral)
                + (AIM_Kd * derivative)
                + (AIM_Ks * Math.signum(error));

        // 6. Clamp final output for motor safety
        return Range.clip(output, -1, 1);
    }

    /**
     * Transforms xVector and yVector from Robot Reference Frame to Turret Reference Frame.
     *
     * @param xVector X vector of the robot's motion in inches per second
     * @param yVector Y vector of the robot's motion in inches per second
     * @param turretAngle Current angle of the turret relative to the robot
     * @return TurretRobotTransform containing the transformed xVector and yVector
     */
    private static TurretRobotTransform transformRobotToTurret(double xVector, double yVector, double turretAngle) {
        double angleRadians = Math.toRadians(turretAngle);
        double cosTheta = Math.cos(angleRadians);
        double sinTheta = Math.sin(angleRadians);

        double transformedXVector = xVector * cosTheta + yVector * sinTheta;
        double transformedYVector = -xVector * sinTheta + yVector * cosTheta;

        return new TurretRobotTransform(transformedXVector, transformedYVector);
    }

    public static class TurretRobotTransform {
        private double xVector;
        private double yVector;
        public TurretRobotTransform(double xVector, double yVector) {
            this.xVector = xVector;
            this.yVector = yVector;
        }
        public double getXVector() {
            return xVector;
        }
        public double getYVector() {
            return yVector;
        }
    }

    /**
     * Calculates the adjusted turret error to target factoring in target bearing, robot movement, and turret angle
     * Implements a PID controller and motion profile to smooth out movment.
     *
     * @param twist twist from pedro follower
     * @param targetBearing current bearing of target from limelight
     * @param turretAngle turret angle on robot
     * @param currentRPM launcher flywheel rpm
     * @param ballLaunchAngle ball launch angle
     * @return TurretRotateCalc containing turretPower, targetBearing, shotLeadAngle, timeOfFlight
     */
    public static TurretRotateCalc calculateTurretLead(Twist twist, double targetBearing, double turretAngle, double currentRPM, double ballLaunchAngle) {
        // Convert Twist velocities from inches per second to meters per second
        double vx_mps = twist.vx * INCHES_TO_METERS;
        double vy_mps = twist.vy * INCHES_TO_METERS;

        TurretRobotTransform turretMovement = transformRobotToTurret(vx_mps, vy_mps, turretAngle);

        double turretPower = 0;

        // Calculate time of flight
        double timeOfFlight = calculateTimeOfFlight(currentRPM, ballLaunchAngle);

        // Calculate shot lead angle
        double shotLeadAngle = calculateShotLead(turretMovement.getXVector(), turretMovement.getYVector(), twist.omega, timeOfFlight);
        //double shotLeadAngle = 0;

        if (Math.abs(targetBearing) > 0) {
            // Desired turret angle is the current bearing plus the shot lead angle
            double adjustedTargetBearing = (targetBearing + shotLeadAngle);

            // Clamp desired turret angle to within -90 and 90 degrees
            if (Math.abs(adjustedTargetBearing + turretAngle) > turretAngleLimit) {
                adjustedTargetBearing = (Math.signum(adjustedTargetBearing) * 90) - turretAngle;
            }

            double turretError = adjustedTargetBearing;

            // Calculate PID control for turret motor
            turretPower = calculatePID(turretError);

            if (Math.abs(turretPower) > 0.01 && Math.abs(adjustedTargetBearing) > AIM_DEADBAND && Math.abs(targetBearing) != 0.0) {
                turretPower = profiledPower.update(turretPower);
            } else {
                turretPower = 0;
            }

        } else {
            turretPower = 0;
            shotLeadAngle = 0;
            timeOfFlight = 0;
        }

        return new TurretRotateCalc(turretPower, targetBearing, shotLeadAngle, timeOfFlight);
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
