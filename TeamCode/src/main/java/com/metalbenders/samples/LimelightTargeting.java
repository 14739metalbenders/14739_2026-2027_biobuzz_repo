package com.metalbenders.samples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@TeleOp(name = "Limelight Targeting", group="sample")
public class LimelightTargeting extends LinearOpMode {


    private Limelight3A limelight;

    @Override
    public void runOpMode() {
        initialize();
        waitForStart();
        while (opModeIsActive()) {
            LLResult llResult = limelight.getLatestResult();
            updateHiveTargeting(llResult);
        }
        limelight.stop();
    }

    private void initialize() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();
    }

    private void updateHiveTargeting(LLResult currentGoal) {

        if (currentGoal == null || !currentGoal.isValid()) {
            return;
        }
        double pitch = Math.toRadians(45); // Camera pitch 45 degrees up
        double[] sumDist3D = new double[4];
        double[] sumDist = new double[4];
        double[] sumX = new double[4];
        double[] sumY = new double[4];
        double[] sumZ = new double[4];
        double[] sumTx = new double[4];
        double[] sumHeight = new double[4];
        double[] sumTy = new double[4];

        int[] count = new int[4];
        int[] count3d = new int[4];
        for (LLResultTypes.FiducialResult tag : currentGoal.getFiducialResults()) {
            int id = tag.getFiducialId();
            int cell = (id - 30) / 4; // 0: red scoring, 1: red audience, 2: blue audience, 3: blue scoring for classing UP/DOWM and Alliance
            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            Pose3D offsetPose = computeCenterWithPoseOffset(id, tag.getTargetPoseCameraSpace());

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

            // If 3D pose get X/Z components and calculate distance
            if (offsetPose != null) {
                Position p = offsetPose.getPosition().toUnit(DistanceUnit.METER);
                if (p.z > 0.05) {
                    double fwd = p.z * Math.cos(pitch) + p.y * Math.sin(pitch);
                    double up = p.z * Math.sin(pitch) - p.y * Math.cos(pitch);
                    //dist3D = Math.hypot(p.z, p.x);
                    sumHeight[cell] += 0.300 + up; // Camera height on robot 0.300m
                    dist3D = Math.hypot(p.x, fwd);
                    sumX[cell] += p.x;
                    sumY[cell] += p.y;
                    sumZ[cell] += p.z;
                    count3d[cell]++;
                }
            }
            // If no 3D pose get use (up cell - camera height) as Z component and calculate distance based on target angle to camera
            dist = (1.261 - 0.300) * camRayHoriz / camRayUp; // Up cell = 1.261m, Camera height on robot 0.300m

            sumDist3D[cell] += dist3D;
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
        double tagDistance3D = sumDist3D[best] / count3d[best];
        double tagDistance = sumDist[best] / count[best];
        double tagX = sumX[best] / count3d[best];
        double tagY = sumY[best] / count3d[best];
        double tagZ = sumZ[best] / count3d[best];
        double aimError = sumTx[best] / count[best];

        telemetry.addData("Target X", tagX);
        telemetry.addData("Target Y", tagY);
        telemetry.addData("Target Z", tagZ);
        telemetry.addData("3D Dist", tagDistance3D);
        telemetry.addData("2D Dist", tagDistance);
        telemetry.addData("Aim Error", aimError);
        telemetry.update();

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

}
