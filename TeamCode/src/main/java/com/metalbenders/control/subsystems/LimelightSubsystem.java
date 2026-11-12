package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.util.TimedEntity;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.metalbenders.constants.Constants.CONVERT_METERS_TO_INCHES;
import static com.metalbenders.constants.Constants.MAX_TIME_TO_COMPARE_TAG_DETECTIONS;
import static com.metalbenders.constants.Constants.MIN_ACTIVE_HIVE_CELL_HEIGHT;
import static com.metalbenders.constants.Constants.MIN_HEIGHT_DIFFERENCE_TO_DETECT_TRANSITION;

public class LimelightSubsystem extends SubsystemBase {

    private static class HiveFrameData {
        private double audienceHeightSum;
        private int audienceHeightCount;
        private double scoringHeightSum;
        private int scoringHeightCount;
        private double toAudienceTransitionStrength;
        private double toScoringTransitionStrength;
    }

    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private final Limelight3A limelight;
    private Constants.LimelightState limelightState;
    private boolean hiveDetected = false;   // did we see hive tags during this scan?
    private double aimError = 0;            // turret error to the up cell visible tags (deg)
    private double tagDistance = 0;             // horizontal camera to tag cluster (m)
    private FieldSide activeHiveCellFieldSide;
    private Constants.HiveState hiveState;
    private final Map<AprilTag, TimedEntity<Double>> previousTagHeights = new HashMap<>();

    public LimelightSubsystem(
            HardwareMap hardwareMap,
            Telemetry telemetry,
            Constants.AllianceColor allianceColor,
            FieldSide activeHiveCellFieldSide) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");
        this.activeHiveCellFieldSide = activeHiveCellFieldSide;
        if (activeHiveCellFieldSide == FieldSide.AUDIENCE) {
            hiveState = Constants.HiveState.ACTIVE_AUDIENCE;
        } else {
            hiveState = Constants.HiveState.ACTIVE_SCORING;
        }
        limelight.pipelineSwitch(0);
        limelight.start();
        setState();
    }

    @Override
    public void periodic() {
        LLResult llResult = limelight.getLatestResult();
        if (isResultValid(llResult)) {
            updateHiveTargeting(llResult.getFiducialResults());
        } else {
            hiveDetected = false;
            aimError = 0;
            tagDistance = 0;
        }
        setState();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Limelight State", limelightState);
        telemetry.addData("Target Distance (in)", tagDistance * CONVERT_METERS_TO_INCHES);
        telemetry.addData("Aim Error (deg)", aimError);
        telemetry.addData("Hive State", hiveState);
        telemetry.addLine();
    }

    /**
     * Calculate hive cell targeting information from limelight.
     * Uses Pose3D from limelight to calculate chich cell is up and uses apriltags to locate the cell center.
     * Provides distance and bearing information to upper alliance hive.
     */
    private void updateHiveTargeting(List<LLResultTypes.FiducialResult> latestDetections) {

        hiveDetected = false;
        long nowMs = System.currentTimeMillis();
        HiveFrameData frameData = new HiveFrameData();

        double pitch = Math.toRadians(45); // Camera pitch 45 degrees up
        double[] sumDist = new double[4];
        double[] sumTx = new double[4];
        double[] sumHeight = new double[4];
        double[] sumTy = new double[4];
        int[] count = new int[4];
        int[] count3d = new int[4];
        int[] distanceCount = new int[4];
        for (FiducialResult tag : latestDetections) {
            int id = tag.getFiducialId();
            AprilTag aprilTag = AprilTag.fromId(id);
            if (aprilTag == null || allianceColor != aprilTag.getAllianceColor()) {
                continue;
            }

            int cell = (id - 30) / 4; // 0: red scoring, 1: red audience, 2: blue audience, 3: blue scoring for classing UP/DOWM and Alliance
            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            Pose3D pose = tag.getTargetPoseCameraSpace();
            Pose3D offsetPose = computeCenterWithPoseOffset(id, pose);
            if (offsetPose == null) {
                continue;
            }

            // Extract the raw 3D linear coordinates (typically in meters or mm)
            double x = offsetPose.getPosition().x; // Left/Right
            double y = offsetPose.getPosition().y; // Up/Down
            double z = offsetPose.getPosition().z; // Distance Forward

            // Convert the 3D position back into 2D degrees using Math.atan2
            double offsetTx = Math.toDegrees(Math.atan2(x, z));
            double offsetTy = Math.toDegrees(Math.atan2(y, z));

            double tanTx = Math.tan(Math.toRadians(offsetTx));
            double tanTy = Math.tan(Math.toRadians(offsetTy));
            double camRayFwd = Math.cos(pitch) - tanTy * Math.sin(pitch);

            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            double bearing = -Math.toDegrees(Math.atan2(tanTx, camRayFwd));  // Bearing from camera to tag center
            double dist = 0.0;
            boolean hasDistance = false;

            double midHeight = (1.261 + 0.905) / 2.0; // Up cell = 1.261m, Down Cell = 0.905m, Midpoint between 2 positions is 1.083m

            // If 3D pose get X/Z components and calculate distance
            Position p = offsetPose.getPosition().toUnit(DistanceUnit.METER);
            if (p.z > 0.05) {
                double fwd = p.z * Math.cos(pitch) + p.y * Math.sin(pitch);
                double up = p.z * Math.sin(pitch) - p.y * Math.cos(pitch);
                double height = 0.300 + up;
                //dist3D = Math.hypot(p.z, p.x);
                if (height > midHeight) {
                    sumHeight[cell] += height; // Camera height on robot 0.300m
                    dist = Math.hypot(p.x, fwd);
                    count3d[cell]++;
                    hasDistance = true;
                }
                observeTagForHiveState(aprilTag, height, nowMs, frameData);
            }

            if (hasDistance) {
                sumDist[cell] += dist;
                distanceCount[cell]++;
            }
            sumTx[cell] += bearing;
            sumTy[cell] += tag.getTargetYDegrees();
            count[cell]++;
        }

        updateHiveStateFromFrame(frameData);

        // Find up cell based on the higher tag cluster (measured height when 3D is available)

        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int c = 0; c < 4; c++) {
            if (count[c] == 0) {
                continue;
            }
            double score;
            if (count3d[c] > 0) {
                // If 3D pose available score for biggest height from 3D data
                double height = sumHeight[c] / count3d[c];

                score = 1000 + height;
            } else {
                // If 3D pose not available score for biggest Ty
                score = sumTy[c] / count[c];
            }
            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        if (best < 0) {
            hiveDetected = false;
            aimError = 0;
            tagDistance = 0;
            return;
        }
        hiveDetected = true;
        tagDistance = distanceCount[best] > 0 ? (sumDist[best] / distanceCount[best]) : 0;
        aimError = sumTx[best] / count[best];
    }

    /**
     * Transforms the known static cell-center offset into Camera Space
     *
     * @param tagId            The visible AprilTag ID
     * @param tagInCameraSpace The Limelight fiducial target object
     * @return Pose3D representing the exact position and orientation of the cell center relative to the camera lens
     */
    private Pose3D computeCenterWithPoseOffset(int tagId, Pose3D tagInCameraSpace) {

        if (tagInCameraSpace == null) return null;

        // Extract position and orientation data fields
        Position tagPos = tagInCameraSpace.getPosition().toUnit(DistanceUnit.METER);
        YawPitchRollAngles tagAngles = tagInCameraSpace.getOrientation();

        // Convert the tag yaw angle to radians for trigonometric rotation math
        double yawRadians = Math.toRadians(tagAngles.getYaw(AngleUnit.DEGREES));

        double xOffset = 0;
        // Determine where the Cell Center lives relative to the Tag's own origin (Tag Space)
        // - If Tag 1 or 3 (Left side of cell face), the center is to its right (+X)
        // - If Tag 2 or 4 (Right side of cell face), the center is to its left (-X)
        if (tagId == 30 || tagId == 34 || tagId == 38 || tagId == 42) {
            xOffset = 0.165;
        }
        if (tagId == 31 || tagId == 35 || tagId == 39 || tagId == 43) {
            xOffset = 0.070;
        }
        if (tagId == 32 || tagId == 36 || tagId == 40 || tagId == 44) {
            xOffset = -0.070;
        }
        if (tagId == 33 || tagId == 37 || tagId == 41 || tagId == 45) {
            xOffset = -0.165;
        }

        // Manually rotate the local X-offset into Camera Space using the tag's Yaw angle
        double correctedX = tagPos.x + (xOffset * Math.cos(yawRadians));
        double correctedZ = tagPos.z + (xOffset * Math.sin(yawRadians));
        double correctedY = tagPos.y; // Assumes cell center and tag share the same vertical height

        // Construct and return a fresh Pose3D containing the newly calculated center position
        return new Pose3D(
                new Position(DistanceUnit.METER, correctedX, correctedY, correctedZ, tagPos.acquisitionTime),
                tagAngles
        );
    }

    private void observeTagForHiveState(
            AprilTag aprilTag,
            double tagHeight,
            long nowMs,
            HiveFrameData frameData) {
        FieldSide fieldSide = aprilTag.getFieldSide();
        if (tagHeight > MIN_ACTIVE_HIVE_CELL_HEIGHT) {
            if (fieldSide == FieldSide.AUDIENCE) {
                frameData.audienceHeightSum += tagHeight;
                frameData.audienceHeightCount++;
            } else if (fieldSide == FieldSide.SCORING) {
                frameData.scoringHeightSum += tagHeight;
                frameData.scoringHeightCount++;
            }
        }

        TimedEntity<Double> previousHeight = previousTagHeights.get(aprilTag);
        previousTagHeights.put(aprilTag, new TimedEntity<>(tagHeight));
        if (previousHeight == null) {
            return;
        }

        long timeDiff = nowMs - previousHeight.getTimestamp();
        if (timeDiff >= MAX_TIME_TO_COMPARE_TAG_DETECTIONS) {
            return;
        }

        double heightDiff = tagHeight - previousHeight.get();
        if (Math.abs(heightDiff) <= MIN_HEIGHT_DIFFERENCE_TO_DETECT_TRANSITION) {
            return;
        }

        if (fieldSide == FieldSide.AUDIENCE && heightDiff > 0) {
            frameData.toAudienceTransitionStrength =
                    Math.max(frameData.toAudienceTransitionStrength, Math.abs(heightDiff));
        } else if (fieldSide == FieldSide.SCORING && heightDiff < 0) {
            frameData.toScoringTransitionStrength =
                    Math.max(frameData.toScoringTransitionStrength, Math.abs(heightDiff));
        }
    }

    private void updateHiveStateFromFrame(HiveFrameData frameData) {
        boolean hasToAudience = frameData.toAudienceTransitionStrength > 0;
        boolean hasToScoring = frameData.toScoringTransitionStrength > 0;

        if (hasToAudience || hasToScoring) {
            if (hasToAudience && hasToScoring) {
                hiveState = frameData.toAudienceTransitionStrength >= frameData.toScoringTransitionStrength
                        ? Constants.HiveState.TRANSITION_TO_AUDIENCE
                        : Constants.HiveState.TRANSITION_TO_SCORING;
            } else if (hasToAudience) {
                hiveState = Constants.HiveState.TRANSITION_TO_AUDIENCE;
            } else {
                hiveState = Constants.HiveState.TRANSITION_TO_SCORING;
            }
            return;
        }

        boolean hasAudienceActive = frameData.audienceHeightCount > 0;
        boolean hasScoringActive = frameData.scoringHeightCount > 0;
        if (!hasAudienceActive && !hasScoringActive) {
            return;
        }

        if (hasAudienceActive && hasScoringActive) {
            double audienceMean = frameData.audienceHeightSum / frameData.audienceHeightCount;
            double scoringMean = frameData.scoringHeightSum / frameData.scoringHeightCount;
            hiveState = audienceMean >= scoringMean
                    ? Constants.HiveState.ACTIVE_AUDIENCE
                    : Constants.HiveState.ACTIVE_SCORING;
            activeHiveCellFieldSide = audienceMean >= scoringMean ? FieldSide.AUDIENCE : FieldSide.SCORING;
            return;
        }

        hiveState = hasAudienceActive
                ? Constants.HiveState.ACTIVE_AUDIENCE
                : Constants.HiveState.ACTIVE_SCORING;
        activeHiveCellFieldSide = hasAudienceActive ? FieldSide.AUDIENCE : FieldSide.SCORING;
    }

    public boolean isHiveDetected() {
        return hiveDetected;
    }

    public double getTargetDistance() {
        if (isHiveDetected()) {
            return tagDistance;
        } else {
            return 0;
        }
    }

    public double getAimError() {
        if (isHiveDetected()) {
            return aimError;
        } else {
            return 0;
        }
    }

    private boolean isResultValid(LLResult llResult) {
        if (llResult == null || !llResult.isValid()) {
            return false;
        }
        return llResult.getFiducialResults() != null && !llResult.getFiducialResults().isEmpty();
    }

    private void setState() {
        if (limelight.isConnected() && limelight.isRunning()) {
            limelightState = Constants.LimelightState.READY;
        } else {
            limelightState = Constants.LimelightState.FAILED;
        }
    }

    public void teardown() {
        limelight.stop();
    }

    public boolean isHealthy() {
        return limelightState == Constants.LimelightState.READY;
    }

    public FieldSide getActiveHiveCellFieldSide() {
        return activeHiveCellFieldSide;
    }

    public Constants.HiveState getHiveState() {
        return hiveState;
    }
}