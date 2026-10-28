package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.Constants;
import com.metalbenders.util.TimedEntity;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.ArrayList;
import java.util.List;

import static com.metalbenders.constants.Constants.FieldSide.AUDIENCE_SIDE;
import static com.metalbenders.constants.Constants.FieldSide.SCORE_SIDE;
import static com.metalbenders.util.MathUtil.calculateAverageTy;

public class LimelightSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private final Limelight3A limelight;
    private Constants.FieldSide activeHiveCellFieldSide;
    private TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections =
            new TimedEntity<>(new ArrayList<>());
    private TimedEntity<List<LLResultTypes.FiducialResult>> lastInactiveSideDetections =
            new TimedEntity<>(new ArrayList<>());

    private boolean hiveDetected = false;   // did we see hive tags during this scan?
    private double aimError = 0;            // turret error to the up cell visible tags (deg)
    private double tagDistance = 0;             // horizontal camera to tag cluster (m)

    public LimelightSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");

        limelight.pipelineSwitch(0);
        limelight.start();

        activeHiveCellFieldSide = (allianceColor == Constants.AllianceColor.RED) ? SCORE_SIDE : AUDIENCE_SIDE;
    }

    private void processLLResult(LLResult llResult) {
        if (!isValidResult(llResult)) {
            return;
        }

        List<LLResultTypes.FiducialResult> scoreSideResults = new ArrayList<>();
        List<LLResultTypes.FiducialResult> audienceSideResults = new ArrayList<>();

        for (LLResultTypes.FiducialResult result : llResult.getFiducialResults()) {
            AprilTag aprilTag = AprilTag.getAprilTagById(result.getFiducialId());
            if (aprilTag == null || aprilTag.getAllianceColor() != allianceColor) {
                continue;
            }

            if (aprilTag.getFieldSide() == SCORE_SIDE) {
                scoreSideResults.add(result);
            } else {
                audienceSideResults.add(result);
            }
        }

        setDetections(scoreSideResults, audienceSideResults);
        updateActiveHiveCellFieldSide();
    }

    private boolean isValidResult(LLResult llResult) {
        return llResult != null
                && llResult.isValid()
                && llResult.getFiducialResults() != null
                && !llResult.getFiducialResults().isEmpty();
    }

    private void setDetections(
            List<LLResultTypes.FiducialResult> scoreSideResults,
            List<LLResultTypes.FiducialResult> audienceSideResults) {
        updateDetectionsForSide(SCORE_SIDE, scoreSideResults);
        updateDetectionsForSide(AUDIENCE_SIDE, audienceSideResults);
    }

    private void updateDetectionsForSide(
            Constants.FieldSide side,
            List<LLResultTypes.FiducialResult> detections) {
        if (detections == null || detections.isEmpty()) {
            return;
        }

        if (activeHiveCellFieldSide == side) {
            lastActiveSideDetections = TimedEntity.of(detections);
        } else {
            lastInactiveSideDetections = TimedEntity.of(detections);
        }
    }

    private void updateActiveHiveCellFieldSide() {
        if (Math.abs(lastActiveSideDetections.getTime() - lastInactiveSideDetections.getTime()) > 500) {
            return;
        }

        double averageTyActiveSide = calculateAverageTy(lastActiveSideDetections.getEntity());
        double averageTyInactiveSide = calculateAverageTy(lastInactiveSideDetections.getEntity());

        // y-values are negative; the lower value is physically higher.
        if (averageTyInactiveSide < averageTyActiveSide) {
            switchActiveSide();
        }
    }

    private void switchActiveSide() {
        activeHiveCellFieldSide = (activeHiveCellFieldSide == SCORE_SIDE) ? AUDIENCE_SIDE : SCORE_SIDE;

        TimedEntity<List<LLResultTypes.FiducialResult>> swap = lastActiveSideDetections;
        lastActiveSideDetections = lastInactiveSideDetections;
        lastInactiveSideDetections = swap;
    }

    public Constants.FieldSide getActiveHiveCellFieldSide() {
        return activeHiveCellFieldSide;
    }

    public void setFieldSideOverride(Constants.FieldSide fieldSideOverride) {
        activeHiveCellFieldSide = fieldSideOverride;
    }

    public TimedEntity<List<LLResultTypes.FiducialResult>> getLastActiveSideDetections() {
        return lastActiveSideDetections;
    }

    @Override
    public void periodic() {
        LLResult llResult = limelight.getLatestResult();
        processLLResult(llResult);
        updateHiveTargeting(llResult, this.allianceColor);

        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        //telemetry.addData("Active Hive Cell Field Side", getActiveHiveCellFieldSide());
        telemetry.addData("Target Distance (in)", tagDistance * 39.3701);
        telemetry.addData("Aim Error (deg)", aimError);
        //addDetectionTelemetry("ACTIVE Side Detections", lastActiveSideDetections);
        //addDetectionTelemetry("INACTIVE Side Detections", lastInactiveSideDetections);


        telemetry.addLine();
    }

    private void addDetectionTelemetry(
            String header,
            TimedEntity<List<LLResultTypes.FiducialResult>> detections) {
        telemetry.addLine(header);
        telemetry.addData("Time Since Last Detection",
                System.currentTimeMillis() - detections.getTime() + " ms");

        for (LLResultTypes.FiducialResult result : detections.getEntity()) {
            telemetry.addLine("\t" + result.getFiducialId());
        }
    }

    private void updateHiveTargeting(LLResult currentGoal, Constants.AllianceColor allianceColor) {

        int hiveTagCount;               // how many of cell tags were used
        hiveDetected = false;

        if (currentGoal == null || !currentGoal.isValid()) {
            return;
        }
        double pitch = Math.toRadians(45); // Camera pitch 45 degrees up
        double[] sumDist = new double[4];
        double[] sumTx = new double[4];
        double[] sumHeight = new double[4];
        double[] sumTy = new double[4];
        int[] count = new int[4];
        int[] count3d = new int[4];
        for (FiducialResult tag : currentGoal.getFiducialResults()) {
            int id = tag.getFiducialId();
            if (!correctAllianceTag(id, allianceColor)) {
                continue;
            }
            int cell = (id - 30) / 4; // 0: red scoring, 1: red audience, 2: blue audience, 3: blue scoring for classing UP/DOWM and Alliance
            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            Pose3D pose = tag.getTargetPoseCameraSpace();
            Pose3D offsetPose = computeCenterWithPoseOffset(id, pose);

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
            double camRayUp = Math.sin(pitch) + tanTy * Math.cos(pitch);
            double camRayHoriz = Math.hypot(tanTx, camRayFwd);

            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            double bearing = -Math.toDegrees(Math.atan2(tanTx, camRayFwd));  // Bearing from camera to tag center
            double dist = Double.NaN; // create dist
            double dist3D = Double.NaN; // create dist

            double midHeight = (1.261 + 0.905) / 2.0; // Up cell = 1.261m, Down Cell = 0.905m, Midpoint between 2 positions is 1.083m

            // If 3D pose get X/Z components and calculate distance
            if (pose != null) {
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
                    }
                }
            }

            //if (Double.isNaN(dist)) { // no 3D pose: assume this cluster is on the upward CELL
            //    if (camRayUp <= 0.01) {
            //        continue;
            //    }
            //    dist = (1.261 - 0.300) * camRayHoriz / camRayUp; // Up cell = 1.261m, Camera height on robot 0.300m
            //}

            sumDist[cell] += dist;
            sumTx[cell] += bearing;
            sumTy[cell] += tag.getTargetYDegrees();
            count[cell]++;
        }

        // Find up cell based on the higher tag cluster (measured height when 3D is available, else larger ty)

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
        hiveTagCount = count[best];
        tagDistance = sumDist[best] / count[best];
        aimError = sumTx[best] / count[best];

    }

    /**
     * Transforms the known static cell-center offset into Camera Space without using .compose()
     *
     * @param tagId The visible AprilTag ID
     * @param tagInCameraSpace The Limelight fiducial target object
     * @return A Pose3D representing the exact position and orientation of the cell center relative to the camera lens
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
        if (tagId == 30 || tagId == 34 || tagId == 38 || tagId == 42) { xOffset = 0.165; }
        if (tagId == 31 || tagId == 35 || tagId == 39 || tagId == 43) { xOffset = 0.070; }
        if (tagId == 32 || tagId == 36 || tagId == 40 || tagId == 44) { xOffset = -0.070; }
        if (tagId == 33 || tagId == 37 || tagId == 41 || tagId == 45) { xOffset = -0.165; }

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


    public boolean isHiveDetected() { return hiveDetected; }
    public double getTargetDistance() { if (isHiveDetected()) { return tagDistance; } else { return 0; } }
    public double getAimError() { if (isHiveDetected()) { return aimError; } else { return 0; } }

    private static boolean correctAllianceTag(int id, Constants.AllianceColor color) {
        return color == Constants.AllianceColor.RED ? (id >= 30 && id <= 37) : (id >= 38 && id <= 45);
    }

    public void teardown() {
        limelight.stop();
    }
}