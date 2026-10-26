package com.metalbenders.samples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

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
            double tanTx = Math.tan(Math.toRadians(tag.getTargetXDegrees()));
            double tanTy = Math.tan(Math.toRadians(tag.getTargetYDegrees()));
            double camRayFwd = Math.cos(pitch) - tanTy * Math.sin(pitch);
            double camRayUp = Math.sin(pitch) + tanTy * Math.cos(pitch);
            double camRayHoriz = Math.hypot(tanTx, camRayFwd);
            // Rotate Tag location from camera reference frame to robot reference frame ----------------------
            double bearing = Math.toDegrees(Math.atan2(tanTx, camRayFwd));  // Bearing from camera to tag center
            double dist = Double.NaN; // create dist
            double dist3D = Double.NaN; // create dist
            Pose3D pose = tag.getTargetPoseRobotSpace();
            // If 3D pose get X/Z components and calculate distance
            if (pose != null) {
                Position p = pose.getPosition().toUnit(DistanceUnit.METER);
                if (p.z > 0.05) {
                    //double fwd = p.x * Math.cos(pitch) + p.y * Math.sin(pitch);
                    //double up = p.x * Math.sin(pitch) - p.y * Math.cos(pitch);
                    dist3D = Math.hypot(p.z, p.x);
                    //sumHeight[cell] += 0.300 + up; // Camera height on robot 0.300m
                    //dist3D = Math.hypot(p.x, p.y);
                    sumX[cell] += p.x;
                    sumY[cell] += p.y;
                    sumZ[cell] += p.z;
                    sumHeight[cell] += p.z;
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


}
