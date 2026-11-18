package com.metalbenders.util;

import com.metalbenders.constants.LimelightLocalizationConstants;
import com.metalbenders.constants.LimelightLocalizationConstants.TagGeometry;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Localizes a level robot from the fixed pivot axes of the moving BIOBUZZ hives.
 * The caller must add synchronized odometry and turret samples using
 * {@link System#currentTimeMillis()} and then call {@link #estimate(LLResult)}.
 * The returned Pedro pose is extrapolated to just before the method returns, assuming
 * constant field-relative velocity between the last two odometry samples.
 *
 * <p>Configure {@link LimelightLocalizationConstants} before use. On each control loop,
 * record the current Pedro pose and turret encoder angle using {@link #addSample(long, Pose, double)},
 * then pass the latest Limelight result to {@link #estimate(LLResult)}. Read the pose only
 * when the returned status is {@link Status#OK}; otherwise the pose is null. Use the
 * result promptly and do not reset the odometry frame within the retained history.
 */
public final class LimelightPoseEstimator {
    private static final double INCHES_PER_METER = 1.0 / 0.0254;
    private static final double MIN_AXIS_LENGTH = 1e-6;
    private final long historyMillis;
    private final long maxPredictionMillis;
    private final LongSupplier clock;
    private final Deque<Sample> history = new ArrayDeque<>();
    private double lastFrameTimestamp = Double.NaN;
    private double lastExposureTime;

    /** Why an estimate succeeded or why it could not be calculated. */
    public enum Status {
        /** A pose is available. */
        OK,
        /** Limelight has no valid result, tags, or usable timestamps. */
        INVALID_RESULT,
        /** One or more camera or turret measurements are missing or invalid. */
        INVALID_CAMERA_CONFIGURATION,
        /** The stored samples do not cover the image capture time. */
        INSUFFICIENT_HISTORY,
        /** None of the observed tags has usable geometry configured. */
        NO_CONFIGURED_TAG,
        /** Configured tags did not produce a usable pose or heading. */
        NO_USABLE_TAG,
        /** Recent odometry is too old or is timestamped in the future. */
        STALE_ODOMETRY
    }

    public static final class Estimate {
        public final Status status;
        /** Pedro field pose in inches and radians; null unless status is OK. */
        public final Pose pose;
        /** Number of tag observations included in the pose. */
        public final int tagsUsed;
        /** Time represented by pose, in System.currentTimeMillis() milliseconds, or -1 on failure. */
        public final long poseTimestampMillis;

        private Estimate(Status status, Pose pose, int tagsUsed, long poseTimestampMillis) {
            this.status = status;
            this.pose = pose;
            this.tagsUsed = tagsUsed;
            this.poseTimestampMillis = poseTimestampMillis;
        }
    }

    static final class Sample {
        // All three measurements describe the same moment in the control loop.
        final double time;
        final Pose pose;
        final double turretRadians;

        Sample(double time, Pose pose, double turretRadians) {
            this.time = time;
            this.pose = pose;
            this.turretRadians = turretRadians;
        }
    }

    /**
     * @param historyMillis history window long enough to cover camera and transport latency
     * @param maxPredictionMillis maximum time to extrapolate beyond the latest odometry sample
     */
    public LimelightPoseEstimator(long historyMillis, long maxPredictionMillis) {
        this(historyMillis, maxPredictionMillis, System::currentTimeMillis);
    }

    LimelightPoseEstimator(long historyMillis, long maxPredictionMillis, LongSupplier clock) {
        if (historyMillis <= 0) {
            throw new IllegalArgumentException("historyMillis must be positive");
        }
        if (maxPredictionMillis < 0) {
            throw new IllegalArgumentException("maxPredictionMillis must not be negative");
        }
        this.historyMillis = historyMillis;
        this.maxPredictionMillis = maxPredictionMillis;
        this.clock = clock;
    }

    /**
     * Record simultaneous measurements, in Pedro inches/radians and turret encoder degrees.
     * Timestamps must use System.currentTimeMillis() and increase strictly.
     */
    public void addSample(long timestampMillis, Pose odometry, double turretDegrees) {
        if (odometry == null || !finite(odometry.x(), odometry.y(), odometry.heading(), turretDegrees)) {
            throw new IllegalArgumentException("odometry and turret angle must be finite");
        }
        if (!history.isEmpty() && timestampMillis <= history.getLast().time) {
            throw new IllegalArgumentException("sample timestamps must increase");
        }
        // Copy the pose so later changes to the caller's odometry cannot alter history.
        history.addLast(new Sample(timestampMillis,
                new Pose(odometry.x(), odometry.y(), odometry.heading()),
                Math.toRadians(turretDegrees)));
        // Keep one sample immediately before the cutoff so interpolation remains possible.
        while (history.size() > 2) {
            java.util.Iterator<Sample> iterator = history.iterator();
            iterator.next();
            if (iterator.next().time >= timestampMillis - historyMillis) break;
            history.removeFirst();
        }
    }

    /**
     * Estimated image time is the Control Hub receipt timestamp minus camera capture and
     * pipeline latency. Network/polling delay is not measured by the SDK. Repeated polls
     * of the same frame retain its first receipt time instead of making it appear newer.
     * The final prediction assumes constant velocity; it is rejected if the latest sample
     * is in the future or older than maxPredictionMillis, or if the two samples used to
     * estimate velocity are more than historyMillis apart.
     * Failures return a status and a null pose; no previous pose is substituted.
     */
    public Estimate estimate(LLResult result) {
        // 1. Check that the camera returned a usable frame and the mounting is measured.
        if (result == null || !result.isValid() || result.getFiducialResults() == null
                || result.getFiducialResults().isEmpty()
                || !finite(result.getCaptureLatency(), result.getTargetingLatency(), result.getTimestamp())
                || result.getCaptureLatency() < 0 || result.getTargetingLatency() < 0
                || result.getControlHubTimeStamp() <= 0 || result.getTimestamp() <= 0) {
            return failure(Status.INVALID_RESULT);
        }
        if (!cameraConfigured()) return failure(Status.INVALID_CAMERA_CONFIGURATION);
        // 2. Approximate when the image was taken. Keep the first receipt time when
        // Limelight returns the same image again, rather than treating it as a new image.
        if (result.getTimestamp() != lastFrameTimestamp) {
            lastFrameTimestamp = result.getTimestamp();
            lastExposureTime = result.getControlHubTimeStamp()
                    - result.getCaptureLatency() - result.getTargetingLatency();
        }
        double exposureTime = lastExposureTime;
        // The image must fall inside the stored history; we do not guess past samples.
        if (history.size() < 2 || exposureTime < history.getFirst().time
                || exposureTime > history.getLast().time
                || history.getLast().time - exposureTime > historyMillis) {
            return failure(Status.INSUFFICIENT_HISTORY);
        }
        Sample exposure = interpolate(exposureTime);
        Sample now = history.getLast();
        List<FiducialResult> detections = result.getFiducialResults();
        double sumX = 0, sumY = 0, sumCos = 0, sumSin = 0;
        int configured = 0, used = 0;
        // 3. Turn each configured tag observation into a robot field pose. Tag IDs
        // 30-45 are accepted regardless of alliance; unknown or unmeasured tags are skipped.
        for (FiducialResult detection : detections) {
            if (detection == null) continue;
            int index = detection.getFiducialId() - 30;
            if (index < 0 || index >= LimelightLocalizationConstants.TAGS.length) continue;
            TagGeometry geometry = LimelightLocalizationConstants.TAGS[index];
            if (!geometryConfigured(geometry)) continue;
            configured++;
            Pose pose = poseFromTag(detection.getTargetPoseCameraSpace(), geometry, exposure.turretRadians);
            if (pose == null) continue;
            sumX += pose.x();
            sumY += pose.y();
            // Average directions as vectors so headings near -180 and +180 degrees
            // average correctly instead of pointing in the opposite direction.
            sumCos += Math.cos(pose.heading());
            sumSin += Math.sin(pose.heading());
            used++;
        }
        if (configured == 0) return failure(Status.NO_CONFIGURED_TAG);
        if (used == 0 || Math.hypot(sumCos, sumSin) < MIN_AXIS_LENGTH) {
            return failure(Status.NO_USABLE_TAG);
        }
        // 4. Average the tag poses, then apply the measured odometry movement from
        // image capture to the newest sample, in the vision pose's field orientation.
        Pose vision = new Pose(sumX / used, sumY / used, Math.atan2(sumSin, sumCos));
        Pose atSample = propagate(vision, exposure.pose, now.pose);
        // 5. Use the last two samples to estimate velocity and project the pose to
        // just before returning. This assumes velocity stays constant over that interval.
        java.util.Iterator<Sample> recent = history.descendingIterator();
        recent.next();
        Sample prior = recent.next();
        long predictionTime = clock.getAsLong();
        double predictionMillis = predictionTime - now.time;
        double sampleMillis = now.time - prior.time;
        if (predictionMillis < 0 || predictionMillis > maxPredictionMillis || sampleMillis > historyMillis) {
            return failure(Status.STALE_ODOMETRY);
        }
        // Rotate the odometry displacement into the vision-corrected field frame.
        double fraction = predictionMillis / sampleMillis;
        double frameRotation = angleDifference(atSample.heading(), now.pose.heading());
        double dx = (now.pose.x() - prior.pose.x()) * fraction;
        double dy = (now.pose.y() - prior.pose.y()) * fraction;
        double cos = Math.cos(frameRotation), sin = Math.sin(frameRotation);
        Pose predicted = new Pose(atSample.x() + cos * dx - sin * dy,
                atSample.y() + sin * dx + cos * dy,
                atSample.heading() + fraction * angleDifference(now.pose.heading(), prior.pose.heading()));
        return new Estimate(Status.OK, predicted, used, predictionTime);
    }

    Sample interpolate(double time) {
        // Find the samples on either side of the image and estimate the pose and
        // turret angle between them. Angle differences take the shortest path around.
        Sample previous = null;
        for (Sample sample : history) {
            if (sample.time == time) return sample;
            if (sample.time > time) {
                double f = (time - previous.time) / (sample.time - previous.time);
                Pose p = new Pose(previous.pose.x() + f * (sample.pose.x() - previous.pose.x()),
                        previous.pose.y() + f * (sample.pose.y() - previous.pose.y()),
                        previous.pose.heading() + f * angleDifference(sample.pose.heading(), previous.pose.heading()));
                return new Sample(time, p, previous.turretRadians
                        + f * angleDifference(sample.turretRadians, previous.turretRadians));
            }
            previous = sample;
        }
        throw new IllegalStateException("exposure time is outside history");
    }

    static Pose poseFromTag(Pose3D observed, TagGeometry geometry, double turretRadians) {
        if (observed == null || observed.getPosition() == null || observed.getOrientation() == null) return null;
        Position p = observed.getPosition().toUnit(DistanceUnit.METER);
        YawPitchRollAngles a = observed.getOrientation();
        double yaw = a.getYaw(AngleUnit.RADIANS);
        double pitch = a.getPitch(AngleUnit.RADIANS);
        double roll = a.getRoll(AngleUnit.RADIANS);
        if (!finite(p.x, p.y, p.z, yaw, pitch, roll) || p.z <= 0) return null;

        // The tag can move as the hive tips, but the pivot stays in the same field
        // location. Rotate the measured tag-to-pivot offset into camera coordinates.
        double[] pivotOffset = rotate(new double[]{geometry.pivotTagX, geometry.pivotTagY, geometry.pivotTagZ},
                yaw, pitch, roll);
        double[] pivotCamera = {p.x * INCHES_PER_METER + pivotOffset[0],
                p.y * INCHES_PER_METER + pivotOffset[1], p.z * INCHES_PER_METER + pivotOffset[2]};
        double[] axisCamera = rotate(new double[]{geometry.axisTagX, geometry.axisTagY, geometry.axisTagZ},
                yaw, pitch, roll);
        // Convert the pivot and its axis from camera coordinates to robot coordinates,
        // accounting for camera mounting and the turret angle at image capture.
        double turret = turretRadians * LimelightLocalizationConstants.TURRET_ANGLE_DIRECTION;
        double cameraYaw = turret + Math.toRadians(LimelightLocalizationConstants.CAMERA_YAW);
        double cameraPitch = Math.toRadians(LimelightLocalizationConstants.CAMERA_PITCH);
        double cameraRoll = Math.toRadians(LimelightLocalizationConstants.CAMERA_ROLL);
        double[] pivotRobot = cameraToRobot(pivotCamera, cameraYaw, cameraPitch, cameraRoll);
        double[] axisRobot = cameraToRobot(axisCamera, cameraYaw, cameraPitch, cameraRoll);
        if (Math.hypot(axisRobot[0], axisRobot[1]) < MIN_AXIS_LENGTH) return null;
        // Compare the observed shaft direction with its known field direction to
        // find the robot heading. This assumes the robot is level.
        double heading = Math.atan2(geometry.axisFieldY, geometry.axisFieldX)
                - Math.atan2(axisRobot[1], axisRobot[0]);
        // Shift from the camera lens to the robot center, rotating the lens offset
        // with the turret, then use the known field pivot to find the robot position.
        double c = Math.cos(turret), s = Math.sin(turret);
        pivotRobot[0] += LimelightLocalizationConstants.TURRET_AXIS_X
                + c * LimelightLocalizationConstants.CAMERA_FROM_TURRET_X
                - s * LimelightLocalizationConstants.CAMERA_FROM_TURRET_Y;
        pivotRobot[1] += LimelightLocalizationConstants.TURRET_AXIS_Y
                + s * LimelightLocalizationConstants.CAMERA_FROM_TURRET_X
                + c * LimelightLocalizationConstants.CAMERA_FROM_TURRET_Y;
        double ch = Math.cos(heading), sh = Math.sin(heading);
        return new Pose(geometry.pivotFieldX - (ch * pivotRobot[0] - sh * pivotRobot[1]),
                geometry.pivotFieldY - (sh * pivotRobot[0] + ch * pivotRobot[1]),
                Math.atan2(sh, ch));
    }

    private static double[] cameraToRobot(double[] v, double yaw, double pitch, double roll) {
        // Limelight: X right, Y down, Z forward. Robot: X forward, Y left, Z up.
        return rotate(new double[]{v[2], -v[0], -v[1]}, yaw, pitch, roll);
    }

    private static double[] rotate(double[] v, double yaw, double pitch, double roll) {
        // Apply roll, then pitch, then yaw to the input vector.
        double cr = Math.cos(roll), sr = Math.sin(roll);
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        double x = cp * v[0] + sp * (sr * v[1] + cr * v[2]);
        double y = cr * v[1] - sr * v[2];
        double z = -sp * v[0] + cp * (sr * v[1] + cr * v[2]);
        return new double[]{cy * x - sy * y, sy * x + cy * y, z};
    }

    static Pose propagate(Pose vision, Pose exposure, Pose now) {
        // Odometry and vision can disagree about field heading. Rotate the odometry
        // displacement by that difference before adding it to the vision position.
        double delta = angleDifference(vision.heading(), exposure.heading());
        double dx = now.x() - exposure.x(), dy = now.y() - exposure.y();
        return new Pose(vision.x() + Math.cos(delta) * dx - Math.sin(delta) * dy,
                vision.y() + Math.sin(delta) * dx + Math.cos(delta) * dy,
                vision.heading() + angleDifference(now.heading(), exposure.heading()));
    }

    private static double angleDifference(double a, double b) {
        // Signed shortest rotation from b to a, including across the +/-pi boundary.
        return Math.atan2(Math.sin(a - b), Math.cos(a - b));
    }

    private static boolean cameraConfigured() {
        return finite(LimelightLocalizationConstants.TURRET_AXIS_X, LimelightLocalizationConstants.TURRET_AXIS_Y,
                LimelightLocalizationConstants.CAMERA_FROM_TURRET_X, LimelightLocalizationConstants.CAMERA_FROM_TURRET_Y,
                LimelightLocalizationConstants.CAMERA_YAW,
                LimelightLocalizationConstants.CAMERA_PITCH, LimelightLocalizationConstants.CAMERA_ROLL)
                && Math.abs(LimelightLocalizationConstants.TURRET_ANGLE_DIRECTION) == 1;
    }

    private static boolean geometryConfigured(TagGeometry g) {
        return g != null && finite(g.pivotFieldX, g.pivotFieldY, g.pivotTagX, g.pivotTagY, g.pivotTagZ,
                g.axisTagX, g.axisTagY, g.axisTagZ, g.axisFieldX, g.axisFieldY)
                && Math.hypot(Math.hypot(g.axisTagX, g.axisTagY), g.axisTagZ) > MIN_AXIS_LENGTH
                && Math.hypot(g.axisFieldX, g.axisFieldY) > MIN_AXIS_LENGTH;
    }

    private static boolean finite(double... values) {
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) return false;
        }
        return true;
    }

    private static Estimate failure(Status status) {
        return new Estimate(status, null, 0, -1);
    }
}
