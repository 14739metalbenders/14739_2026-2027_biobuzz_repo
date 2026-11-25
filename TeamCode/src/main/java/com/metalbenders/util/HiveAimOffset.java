package com.metalbenders.util;

import com.metalbenders.constants.HiveAimOffsetConfig;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/** Face-relative offsets for the already-selected cell, independent of field pose. */
public final class HiveAimOffset {
    private static final double INCHES_TO_METERS = 0.0254;
    private static final double MIN_HORIZONTAL = 0.1;
    private static final double MAX_AXIS_TILT = 0.15;

    private HiveAimOffset() {
    }

    public static final class Result {
        public final double viewingAngle, offsetInches, bearing;
        public final String error;

        private Result(double viewingAngle, double offsetInches, double bearing, String error) {
            this.viewingAngle = viewingAngle;
            this.offsetInches = offsetInches;
            this.bearing = bearing;
            this.error = error;
        }
    }

    /** One cell's observations; an invalid observation makes the whole cell use center aim. */
    public static final class Aggregate {
        private double centerSum, bearingSum, angleSum, offsetSum;
        private int count;
        private String error;

        public void addCenter(double centerBearing) {
            centerSum += centerBearing;
            count++;
            if (error == null) error = "Offset not calculated";
        }

        public void add(double centerBearing, Result result) {
            centerSum += centerBearing;
            count++;
            if (result == null || result.error != null) {
                if (error == null) error = result == null ? "Missing offset result" : result.error;
            } else {
                bearingSum += result.bearing;
                angleSum += result.viewingAngle;
                offsetSum += result.offsetInches;
            }
        }

        public Result result() {
            return result(true);
        }

        public Result result(boolean enabled) {
            if (count == 0) return failure("No target");
            if (!enabled) return new Result(Double.NaN, Double.NaN, centerSum / count, null);
            if (error != null) return new Result(Double.NaN, Double.NaN, centerSum / count, error);
            return new Result(angleSum / count, offsetSum / count, bearingSum / count, null);
        }
    }

    public static Result calculate(Pose3D center, HiveAimOffsetConfig.Table table,
                                   double cameraPitchDegrees, double cameraRollDegrees) {
        if (center == null || center.getPosition() == null || center.getOrientation() == null) {
            return failure("Missing tag pose");
        }
        Position p = center.getPosition().toUnit(DistanceUnit.METER);
        YawPitchRollAngles a = center.getOrientation();
        double yaw = a.getYaw(AngleUnit.RADIANS), pitch = a.getPitch(AngleUnit.RADIANS);
        double roll = a.getRoll(AngleUnit.RADIANS);
        if (!finite(p.x, p.y, p.z, yaw, pitch, roll, cameraPitchDegrees, cameraRollDegrees)
                || p.z <= 0.05) {
            return failure("Invalid tag pose or camera mounting");
        }
        double[] cameraCenter = {p.x, p.y, p.z};
        double[] rightCamera = rotate(new double[]{1, 0, 0}, yaw, pitch, roll);
        double[] normalCamera = rotate(new double[]{0, 0, 1}, yaw, pitch, roll);
        double cp = Math.toRadians(cameraPitchDegrees), cr = Math.toRadians(cameraRollDegrees);
        double[] levelCenter = cameraToLevel(cameraCenter, cp, cr);
        double[] right = cameraToLevel(rightCamera, cp, cr);
        double[] normal = cameraToLevel(normalCamera, cp, cr);
        double rightLength = Math.hypot(right[0], right[1]);
        double normalLength = Math.hypot(normal[0], normal[1]);
        double distance = Math.hypot(levelCenter[0], levelCenter[1]);
        if (rightLength < MIN_HORIZONTAL || normalLength < MIN_HORIZONTAL || distance < 0.05
                || Math.abs(right[2]) > MAX_AXIS_TILT) {
            return failure("Face direction is not reliably horizontal");
        }
        right[0] /= rightLength;
        right[1] /= rightLength;
        // Remove any horizontal component parallel to the face axis after projection.
        double alongRight = normal[0] * right[0] + normal[1] * right[1];
        normal[0] -= alongRight * right[0];
        normal[1] -= alongRight * right[1];
        normalLength = Math.hypot(normal[0], normal[1]);
        if (normalLength < MIN_HORIZONTAL) return failure("Face normal is ambiguous");
        normal[0] /= normalLength;
        normal[1] /= normalLength;
        double cameraRight = -levelCenter[0] * right[0] - levelCenter[1] * right[1];
        double cameraOut = -levelCenter[0] * normal[0] - levelCenter[1] * normal[1];
        if (cameraOut <= 0) return failure("Camera is behind the tag face");
        double angle = Math.toDegrees(Math.atan2(cameraRight, cameraOut));
        double offset = interpolate(table, angle);
        if (!Double.isFinite(offset)) return failure("Invalid offset table");

        // Positive offset is left when looking at the cell, opposite tag +X.
        double[] shiftedLevel = {levelCenter[0] - right[0] * offset * INCHES_TO_METERS,
                levelCenter[1] - right[1] * offset * INCHES_TO_METERS, levelCenter[2]};
        double[] shifted = offset == 0 ? cameraCenter : levelToCamera(shiftedLevel, cp, cr);
        if (!finite(shifted) || shifted[2] <= 0.05) return failure("Adjusted target behind camera");
        // Keep the existing targeting pitch and sign convention.
        double tx = Math.atan2(shifted[0], shifted[2]);
        double ty = Math.atan2(shifted[1], shifted[2]);
        double fwd = Math.cos(Math.toRadians(45)) - Math.tan(ty) * Math.sin(Math.toRadians(45));
        double bearing = -Math.toDegrees(Math.atan2(Math.tan(tx), fwd));
        if (!Double.isFinite(bearing)) return failure("Invalid adjusted bearing");
        return new Result(angle, offset, bearing, null);
    }

    public static double interpolate(HiveAimOffsetConfig.Table table, double angle) {
        if (table == null || !Double.isFinite(angle)) return Double.NaN;
        double a = table.angle1, b = table.angle2, c = table.angle3;
        double x = table.offset1, y = table.offset2, z = table.offset3;
        if (!finite(a, b, c, x, y, z) || a >= b || b >= c) return Double.NaN;
        if (angle <= a) return x;
        if (angle >= c) return z;
        if (angle <= b) return x + (y - x) * ((angle - a) / (b - a));
        return y + (z - y) * ((angle - b) / (c - b));
    }

    private static Result failure(String error) {
        return new Result(Double.NaN, Double.NaN, Double.NaN, error);
    }

    private static boolean finite(double... values) {
        for (double value : values) if (!Double.isFinite(value)) return false;
        return true;
    }

    private static double[] cameraToLevel(double[] v, double pitch, double roll) {
        return rotate(new double[]{v[2], -v[0], -v[1]}, 0, pitch, roll);
    }

    private static double[] levelToCamera(double[] v, double pitch, double roll) {
        double c = Math.cos(pitch), s = Math.sin(pitch);
        double x = c * v[0] - s * v[2];
        double y = Math.cos(roll) * v[1] + Math.sin(roll) * (s * v[0] + c * v[2]);
        double z = -Math.sin(roll) * v[1] + Math.cos(roll) * (s * v[0] + c * v[2]);
        return new double[]{-y, -z, x};
    }

    private static double[] rotate(double[] v, double yaw, double pitch, double roll) {
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
        double cr = Math.cos(roll), sr = Math.sin(roll);
        return new double[]{
                v[0] * cy * cp + v[1] * (cy * sp * sr - sy * cr) + v[2] * (cy * sp * cr + sy * sr),
                v[0] * sy * cp + v[1] * (sy * sp * sr + cy * cr) + v[2] * (sy * sp * cr - cy * sr),
                -v[0] * sp + v[1] * cp * sr + v[2] * cp * cr};
    }
}
