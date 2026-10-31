package com.metalbenders.util;

import com.qualcomm.hardware.limelightvision.LLResultTypes;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.List;

public class Localization {

    private static final double METERS_TO_INCHES = 39.3701;
    private static final double HIVE_CENTER_Z_METERS = 1.116;

    // --- MECHANICAL CONFIGURATION CONSTANTS (INCHES) ---
    private static final double CAMERA_RADIUS_FROM_TURRET_CENTER = 4.5;
    private static final double TURRET_FIXED_OFFSET_X = 2.0; // Positive if forward of center
    private static final double TURRET_FIXED_OFFSET_Y = 0.0; // Positive if left of center

    public enum TagOffset {
        tag30(30,-0.158,-0.323, -50.0, 50.0), // Example: ID, local X, local Y, Field Center X, Field Center Y (inches)
        tag31(31,-0.253,-0.323, -50.0, 50.0),
        tag32(32,-0.393,-0.323, -50.0, 50.0),
        tag33(33,-0.488,-0.323, -50.0, 50.0),
        tag34(34,0.488,-0.323,  12.0, 12.0), // Closest to origin example
        tag35(35,0.393,-0.323,  50.0, -50.0),
        tag36(36,0.253,-0.323,  50.0, -50.0),
        tag37(37,0.158,-0.323,  50.0, -50.0),
        tag38(38,-0.158,-0.323, -50.0, -50.0),
        tag39(39,-0.253,-0.323, -50.0, -50.0),
        tag40(40,-0.393,-0.323, -50.0, -50.0),
        tag41(41,-0.488,-0.323, -50.0, -50.0),
        tag42(42,0.488,-0.323,  65.0, 65.0),  // Furthest away example
        tag43(43,0.393,-0.323,  65.0, 65.0),
        tag44(44,0.253,-0.323,  65.0, 65.0),
        tag45(45,0.158,-0.323,  65.0, 65.0);

        private final int id;
        private final double xOffset; // tag space meters
        private final double yOffset; // tag space meters
        private final double hiveFieldX; // Pedro Field space inches
        private final double hiveFieldY; // Pedro Field space inches

        TagOffset(int id, double xOffset, double yOffset, double hiveFieldX, double hiveFieldY) {
            this.id = id;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.hiveFieldX = hiveFieldX;
            this.hiveFieldY = hiveFieldY;
        }

        public int getId() { return id; }
        public double getXOffset() { return xOffset; }
        public double getYOffset() { return yOffset; }
        public double getHiveFieldX() { return hiveFieldX; }
        public double getHiveFieldY() { return hiveFieldY; }

        public static TagOffset fromId(int id) {
            for (TagOffset tag : values()) {
                if (tag.id == id) return tag;
            }
            return null;
        }
    }

    /**
     * Iterates across all visible multi-tag outputs from Limelight.
     * Computes unique positions handling upside-down targets, and fuses them into an average.
     *
     * @param dynamicTagResults  The raw list of parsed fiducial detections from Limelight.
     * @param turretAngleDegrees Current physical angle of your turret relative to chassis forward.
     * @return A single fused global Pose3D field location for the robot floor center.
     */
    public static Pose3D localizeRobotWithMultiTags(List<LLResultTypes.FiducialResult> dynamicTagResults, double turretAngleDegrees) {
        if (dynamicTagResults == null || dynamicTagResults.isEmpty()) return null;

        double sumX = 0;
        double sumY = 0;
        double sumHeadingX = 0; // Component accumulator for angular circular averaging
        double sumHeadingY = 0;
        double totalWeight = 0;
        long timestamp = 0;

        for (LLResultTypes.FiducialResult fiducial : dynamicTagResults) {
            int id = fiducial.getFiducialId();
            TagOffset offsetInfo = TagOffset.fromId(id);
            if (offsetInfo == null) continue;

            Pose3D tagInCameraSpace = fiducial.getTargetPoseCameraSpace();
            if (tagInCameraSpace == null) continue;

            // --- STEP 1: Full 3D Matrix Inversion ---
            // Naturally compensates for flipped, sideways, or upside-down orientations
            Position pos = tagInCameraSpace.getPosition().toUnit(DistanceUnit.METER);
            YawPitchRollAngles orientation = tagInCameraSpace.getOrientation();
            timestamp = orientation.getAcquisitionTime();

            double yawRad = Math.toRadians(orientation.getYaw(AngleUnit.DEGREES));
            double pitchRad = Math.toRadians(orientation.getPitch(AngleUnit.DEGREES));
            double rollRad = Math.toRadians(orientation.getRoll(AngleUnit.DEGREES));

            double cx = Math.cos(rollRad),   sx = Math.sin(rollRad);
            double cy = Math.cos(pitchRad),  sy = Math.sin(pitchRad);
            double cz = Math.cos(yawRad),   sz = Math.sin(yawRad);

            // 3D rotation transpose matrix mapping elements
            double r00 = cz * cy;   double r10 = sz * cy;   double r20 = -sy;
            double r01 = cz * sy * sx - sz * cx; double r11 = sz * sy * sx + cz * cx; double r21 = cy * sx;
            double r02 = cz * sy * cx + sz * sx; double r12 = sz * sy * cx - cz * sx; double r22 = cy * cx;

            // Camera location inside local tag space (meters)
            double camInTagX = -(r00 * pos.x + r10 * pos.y + r20 * pos.z);
            double camInTagY = -(r01 * pos.x + r11 * pos.y + r21 * pos.z);

            // --- STEP 2: Shift to Hive Center inside Tag Space ---
            double camInHiveX = (camInTagX - offsetInfo.getXOffset()) * METERS_TO_INCHES;
            double camInHiveY = (camInTagY - offsetInfo.getYOffset()) * METERS_TO_INCHES;

            // --- STEP 3: Handle Pitch & Upside Down Matrix Ambiguity ---
            // If the tag is upside down, the raw yaw vector flips components. We map relative field vectors directly.
            double localCamHeadingRad = Math.atan2(camInHiveY, camInHiveX);
            double fieldDistanceToHive = Math.hypot(camInHiveX, camInHiveY);

            // Compute global orientation by computing angle to target lookups
            // This eliminates rough quadrant estimations entirely.
            double robotRelativeHeadingRad = Math.atan2(-camInTagY, camInTagX);
            double globalCameraHeadingRad = localCamHeadingRad + Math.PI;
            double robotGlobalHeadingRad = globalCameraHeadingRad - Math.toRadians(turretAngleDegrees);

            // Frame rotation shift into Field Space Coordinates
            double cosCam = Math.cos(globalCameraHeadingRad);
            double sinCam = Math.sin(globalCameraHeadingRad);

            double fieldCamX = offsetInfo.getHiveFieldX() - (fieldDistanceToHive * cosCam);
            double fieldCamY = offsetInfo.getHiveFieldY() - (fieldDistanceToHive * sinCam);

            // --- STEP 4: Camera to Turret to Robot Base ---
            double fieldTurretX = fieldCamX - (CAMERA_RADIUS_FROM_TURRET_CENTER * cosCam);
            double fieldTurretY = fieldCamY - (CAMERA_RADIUS_FROM_TURRET_CENTER * sinCam);

            double cosBot = Math.cos(robotGlobalHeadingRad);
            double sinBot = Math.sin(robotGlobalHeadingRad);

            double rotatedOffsetX = TURRET_FIXED_OFFSET_X * cosBot - TURRET_FIXED_OFFSET_Y * sinBot;
            double rotatedOffsetY = TURRET_FIXED_OFFSET_X * sinBot + TURRET_FIXED_OFFSET_Y * cosBot;

            double robotFieldX = fieldTurretX - rotatedOffsetX;
            double robotFieldY = fieldTurretY - rotatedOffsetY;

            // --- STEP 5: Weighted Accumulation (Closer detections have higher reliability) ---
            double weight = 1.0 / (fieldDistanceToHive * fieldDistanceToHive); // Inverse square law weight
            sumX += robotFieldX * weight;
            sumY += robotFieldY * weight;

            // Vector components for angular averaging to avoid wrap-around errors (e.g., averaging 359 and 1)
            sumHeadingX += Math.cos(robotGlobalHeadingRad) * weight;
            sumHeadingY += Math.sin(robotGlobalHeadingRad) * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) return null;

        // Final weighted coordinates
        double finalizedX = (sumX / totalWeight) + 72;
        double finalizedY = (sumY / totalWeight) + 72;
        double finalizedHeading = Math.atan2(sumHeadingY / totalWeight, sumHeadingX / totalWeight);

        return new Pose3D(
                new Position(DistanceUnit.INCH, finalizedX, finalizedY, 0.0, timestamp),
                new YawPitchRollAngles(AngleUnit.DEGREES, Math.toDegrees(finalizedHeading), 0, 0, timestamp)
        );
    }
}
