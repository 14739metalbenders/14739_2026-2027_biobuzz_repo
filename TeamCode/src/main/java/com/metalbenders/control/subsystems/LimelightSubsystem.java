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
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

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
        telemetry.addData("Target Distance (m)", tagDistance);
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
            double tanTx = Math.tan(Math.toRadians(tag.getTargetXDegrees()));
            double tanTy = Math.tan(Math.toRadians(tag.getTargetYDegrees()));
            double camRayFwd = Math.cos(pitch) - tanTy * Math.sin(pitch);
            double camRayUp = Math.sin(pitch) + tanTy * Math.cos(pitch);
            double camRayHoriz = Math.hypot(tanTx, camRayFwd);
            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            double bearing = Math.toDegrees(Math.atan2(tanTx, camRayFwd));  // Bearing from camera to tag center
            double dist = Double.NaN; // create dist
            Pose3D pose = tag.getTargetPoseRobotSpace();
            // If 3D pose get X/Z components and calculate distance
            if (pose != null) {
                Position p = pose.getPosition().toUnit(DistanceUnit.METER);
                if (p.z > 0.05) {
                    //double fwd = p.z * Math.cos(pitch) + p.y * Math.sin(pitch);
                    //double up = p.z * Math.sin(pitch) - p.y * Math.cos(pitch);
                    //dist = Math.hypot(p.x, fwd);
                    //sumHeight[cell] += 0.300 + up; // Camera height on robot 0.300m
                    dist = Math.hypot(p.z, p.x);
                    //dist = Math.hypot(p.x, p.y);
                    sumHeight[cell] += p.z;
                    count3d[cell]++;
                }
            }
            // If no 3D pose get use (up cell - camera height) as Z component and calculate distance based on target angle to camera
            //if (Double.isNaN(dist)) { // no 3D pose: assume this cluster is on the up cell
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
        double midHeight = (1.261 + 0.905) / 2.0; // Up cell = 1.261m, Down Cell = 0.905m, Midpoint between 2 positions is 1.083m
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
                if (height < midHeight) {
                    continue; // this cell is below the mid-point between up/down cells and is not a valid target
                }
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