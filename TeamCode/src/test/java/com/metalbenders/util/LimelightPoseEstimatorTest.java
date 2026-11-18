package com.metalbenders.util;

import com.metalbenders.constants.LimelightLocalizationConstants;
import com.metalbenders.constants.LimelightLocalizationConstants.TagGeometry;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.junit.Before;
import org.junit.Test;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class LimelightPoseEstimatorTest {
    private static final double EPS = 1e-8;
    private static final double INCHES_PER_METER = 1 / 0.0254;

    @Before
    public void configureCamera() {
        LimelightLocalizationConstants.TURRET_AXIS_X = 0;
        LimelightLocalizationConstants.TURRET_AXIS_Y = 0;
        LimelightLocalizationConstants.CAMERA_FROM_TURRET_X = 0;
        LimelightLocalizationConstants.CAMERA_FROM_TURRET_Y = 0;
        LimelightLocalizationConstants.CAMERA_YAW = 0;
        LimelightLocalizationConstants.CAMERA_PITCH = 0;
        LimelightLocalizationConstants.CAMERA_ROLL = 0;
        LimelightLocalizationConstants.TURRET_ANGLE_DIRECTION = 1;
        java.util.Arrays.fill(LimelightLocalizationConstants.TAGS, null);
    }

    private static Pose3D observation(double x, double y, double z, double roll) {
        return new Pose3D(new Position(DistanceUnit.INCH, x, y, z, 0),
                new YawPitchRollAngles(AngleUnit.RADIANS, 0, 0, roll, 0));
    }

    @Test
    public void usesPivotRatherThanMovingTagPosition() {
        TagGeometry g = new TagGeometry(100, 60, 0, 10, 0, 1, 0, 0, 1, 0);
        Pose first = LimelightPoseEstimator.poseFromTag(observation(0, 0, INCHES_PER_METER, 0), g, 0);
        // The hive has rotated 90 degrees about its X axis, without moving the robot or pivot.
        Pose tipped = LimelightPoseEstimator.poseFromTag(
                observation(0, 10, INCHES_PER_METER - 10, Math.PI / 2), g, 0);
        assertEquals(100, first.x(), EPS);
        assertEquals(60 - INCHES_PER_METER, first.y(), EPS);
        assertEquals(Math.PI / 2, first.heading(), EPS);
        assertEquals(first.x(), tipped.x(), EPS);
        assertEquals(first.y(), tipped.y(), EPS);
        assertEquals(first.heading(), tipped.heading(), EPS);
    }

    @Test
    public void rotatesCameraAndOffsetWithTurret() {
        LimelightLocalizationConstants.CAMERA_FROM_TURRET_X = 4;
        TagGeometry g = new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        Pose result = LimelightPoseEstimator.poseFromTag(
                observation(0, 0, INCHES_PER_METER, 0), g, Math.PI / 2);
        assertEquals(100, result.x(), EPS);
        assertEquals(60 - 4 - INCHES_PER_METER, result.y(), EPS);
        assertEquals(0, result.heading(), EPS);
    }

    @Test
    public void accountsForCameraPitch() {
        LimelightLocalizationConstants.CAMERA_PITCH = 30;
        TagGeometry g = new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        Pose result = LimelightPoseEstimator.poseFromTag(
                observation(0, 0, INCHES_PER_METER, 0), g, 0);
        assertEquals(100, result.x(), EPS);
        assertEquals(60 - INCHES_PER_METER * Math.cos(Math.toRadians(30)), result.y(), EPS);
    }

    @Test
    public void compensatesUsingRelativeOdometryInVisionFrame() {
        Pose result = LimelightPoseEstimator.propagate(
                new Pose(10, 20, Math.PI / 2), new Pose(100, 100, 0),
                new Pose(110, 100, Math.PI / 4));
        assertEquals(10, result.x(), EPS);
        assertEquals(30, result.y(), EPS);
        assertEquals(3 * Math.PI / 4, result.heading(), EPS);
    }

    @Test
    public void interpolatesHeadingsAcrossWraparound() {
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 100);
        estimator.addSample(1000, new Pose(0, 0, Math.toRadians(179)), 179);
        estimator.addSample(1100, new Pose(10, 20, Math.toRadians(-179)), -179);
        LimelightPoseEstimator.Sample sample = estimator.interpolate(1050);
        assertEquals(5, sample.pose.x(), EPS);
        assertEquals(10, sample.pose.y(), EPS);
        assertEquals(Math.PI, sample.pose.heading(), EPS);
        assertEquals(Math.PI, sample.turretRadians, EPS);
    }

    @Test
    public void rejectsInvalidImageAndNonIncreasingSamples() {
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(1000, 100);
        assertEquals(LimelightPoseEstimator.Status.INVALID_RESULT, estimator.estimate(null).status);
        estimator.addSample(100, new Pose(0, 0, 0), 0);
        assertThrows(IllegalArgumentException.class,
                () -> estimator.addSample(100, new Pose(0, 0, 0), 0));
    }

    @Test
    public void rejectsAbsentAndBehindCameraTags() {
        TagGeometry g = new TagGeometry(0, 0, 0, 0, 0, 1, 0, 0, 1, 0);
        assertNull(LimelightPoseEstimator.poseFromTag(null, g, 0));
        assertNull(LimelightPoseEstimator.poseFromTag(observation(0, 0, -1, 0), g, 0));
    }

    private static class TestResult extends LLResult {
        long receipt;

        TestResult(String json) throws JSONException {
            super(new JSONObject(json));
            receipt = super.getControlHubTimeStamp();
        }

        @Override
        public long getControlHubTimeStamp() {
            return receipt;
        }
    }

    @Test
    public void estimatesAndPropagatesFiducialResultFromEitherAlliance() throws JSONException {
        String json = "{\"v\":1,\"ts\":123,\"cl\":20,\"tl\":10,\"Fiducial\":["
                + "{\"fID\":30,\"t6t_cs\":[0,0,1,0,0,0]},"
                + "{\"fID\":45,\"t6t_cs\":[0,0,1,0,0,0]}]}";
        LLResult result = new TestResult(json);
        TagGeometry geometry = new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        LimelightLocalizationConstants.TAGS[0] = geometry;
        LimelightLocalizationConstants.TAGS[15] = geometry;
        long receipt = result.getControlHubTimeStamp();
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 100, () -> receipt);
        estimator.addSample(receipt - 40, new Pose(0, 0, 0), 0);
        estimator.addSample(receipt - 20, new Pose(2, 0, 0), 0);
        estimator.addSample(receipt, new Pose(4, 0, 0), 0);
        LimelightPoseEstimator.Estimate estimate = estimator.estimate(result);
        assertEquals(LimelightPoseEstimator.Status.OK, estimate.status);
        assertEquals(2, estimate.tagsUsed);
        assertEquals(100, estimate.pose.x(), EPS);
        assertEquals(63 - INCHES_PER_METER, estimate.pose.y(), EPS);
        assertEquals(Math.PI / 2, estimate.pose.heading(), EPS);
        assertEquals(receipt, estimate.poseTimestampMillis);
    }

    @Test
    public void repeatedFramesRetainOriginalExposureTime() throws JSONException {
        String json = "{\"v\":1,\"ts\":123,\"Fiducial\":["
                + "{\"fID\":30,\"t6t_cs\":[0,0,1,0,0,0]}]}";
        TestResult first = new TestResult(json);
        first.receipt = 1000;
        TestResult repeated = new TestResult(json);
        repeated.receipt = 1100;
        LimelightLocalizationConstants.TAGS[0] =
                new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        AtomicLong clock = new AtomicLong(1100);
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 100, clock::get);
        estimator.addSample(900, new Pose(0, 0, 0), 0);
        estimator.addSample(1000, new Pose(10, 0, 0), 0);
        estimator.addSample(1100, new Pose(20, 0, 0), 0);
        Pose firstPose = estimator.estimate(first).pose;
        estimator.addSample(1200, new Pose(30, 0, 0), 0);
        clock.set(1200);
        Pose secondPose = estimator.estimate(repeated).pose;
        assertEquals(firstPose.x(), secondPose.x(), EPS);
        assertEquals(firstPose.y() + 10, secondPose.y(), EPS);
    }

    @Test
    public void reportsUnconfiguredTagsAndMissingHistory() throws JSONException {
        LLResult result = new TestResult("{\"v\":1,\"ts\":123,\"Fiducial\":["
                + "{\"fID\":45,\"t6t_cs\":[0,0,1,0,0,0]}]}");
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 100);
        assertEquals(LimelightPoseEstimator.Status.INSUFFICIENT_HISTORY, estimator.estimate(result).status);
        long receipt = result.getControlHubTimeStamp();
        estimator.addSample(receipt - 1, new Pose(0, 0, 0), 0);
        estimator.addSample(receipt + 1, new Pose(0, 0, 0), 0);
        assertEquals(LimelightPoseEstimator.Status.NO_CONFIGURED_TAG, estimator.estimate(result).status);
    }

    @Test
    public void predictsMotionAndHeadingToTimeOfUse() throws JSONException {
        TestResult result = new TestResult("{\"v\":1,\"ts\":123,\"Fiducial\":["
                + "{\"fID\":30,\"t6t_cs\":[0,0,1,0,0,0]}]}");
        result.receipt = 1000;
        LimelightLocalizationConstants.TAGS[0] =
                new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        AtomicLong clock = new AtomicLong(1150);
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 50, clock::get);
        estimator.addSample(1000, new Pose(0, 0, Math.toRadians(179)), 0);
        estimator.addSample(1100, new Pose(10, 0, Math.toRadians(-179)), 0);
        LimelightPoseEstimator.Estimate estimate = estimator.estimate(result);
        assertEquals(LimelightPoseEstimator.Status.OK, estimate.status);
        double correction = Math.PI / 2 - Math.toRadians(179);
        assertEquals(100 + 15 * Math.cos(correction), estimate.pose.x(), EPS);
        assertEquals(60 - INCHES_PER_METER + 15 * Math.sin(correction), estimate.pose.y(), EPS);
        assertEquals(Math.PI / 2 + Math.toRadians(3), estimate.pose.heading(), EPS);
        assertEquals(1150, estimate.poseTimestampMillis);

        clock.set(1151);
        estimate = estimator.estimate(result);
        assertEquals(LimelightPoseEstimator.Status.STALE_ODOMETRY, estimate.status);
        assertNull(estimate.pose);
        assertEquals(-1, estimate.poseTimestampMillis);
        clock.set(1099);
        assertEquals(LimelightPoseEstimator.Status.STALE_ODOMETRY, estimator.estimate(result).status);
    }

    @Test
    public void rejectsOldVelocitySamples() throws JSONException {
        TestResult result = new TestResult("{\"v\":1,\"ts\":123,\"Fiducial\":["
                + "{\"fID\":30,\"t6t_cs\":[0,0,1,0,0,0]}]}");
        result.receipt = 2000;
        LimelightLocalizationConstants.TAGS[0] =
                new TagGeometry(100, 60, 0, 0, 0, 1, 0, 0, 1, 0);
        LimelightPoseEstimator estimator = new LimelightPoseEstimator(500, 100, () -> 2000);
        estimator.addSample(1000, new Pose(0, 0, 0), 0);
        estimator.addSample(2000, new Pose(10, 0, 0), 0);
        assertEquals(LimelightPoseEstimator.Status.STALE_ODOMETRY, estimator.estimate(result).status);
    }
}
