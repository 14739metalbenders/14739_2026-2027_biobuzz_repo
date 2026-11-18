package com.metalbenders.samples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.metalbenders.control.subsystems.TurretSubsystem;
import com.metalbenders.util.LimelightPoseEstimator;
import com.metalbenders.util.LimelightPoseEstimator.Estimate;
import com.metalbenders.util.LimelightPoseEstimator.Status;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Place the robot at a measured Pedro field pose and set START_X, START_Y and
 * START_HEADING_DEGREES in Dashboard, then set START_POSE_CONFIRMED to true before
 * pressing start. Configure the Limelight mounting and tag geometry first.
 *
 * Hold gamepad 1 left bumper to drive slowly: left stick translates in the field
 * frame and right stick turns. The turret is not powered by this test. Vision is
 * displayed only; it is never used to reset odometry or control the drivetrain.
 */
@Config
@TeleOp(name = "Limelight Pose Estimator Test", group = "sample")
public class LimelightPoseEstimatorTest extends LinearOpMode {
    public static double START_X = 0;
    public static double START_Y = 0;
    public static double START_HEADING_DEGREES = 0;
    public static boolean START_POSE_CONFIRMED = false;

    private static final long HISTORY_MILLIS = 1000;
    private static final long MAX_PREDICTION_MILLIS = 100;
    private static final double DRIVE_SCALE = 0.25;

    @Override
    public void runOpMode() {
        START_POSE_CONFIRMED = false;
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        Follower follower = null;
        try {
            TurretSubsystem turret = new TurretSubsystem(hardwareMap, telemetry);
            GamepadEx driver = new GamepadEx(gamepad1);
            follower = Constants.create(hardwareMap);
            limelight.pipelineSwitch(0);
            limelight.start();

            while (opModeInInit()) {
                telemetry.addLine("Set measured START_X, START_Y and START_HEADING_DEGREES in Dashboard.");
                telemetry.addLine("Set START_POSE_CONFIRMED to true before starting.");
                telemetry.addLine("Configure camera and tag geometry before testing.");
                telemetry.addLine("Hold gamepad 1 left bumper to drive; turret is not powered.");
                telemetry.addData("Start pose", "x %.2f, y %.2f in, heading %.1f deg",
                        START_X, START_Y, START_HEADING_DEGREES);
                telemetry.addData("Start pose confirmed", START_POSE_CONFIRMED);
                telemetry.addData("Limelight connected", limelight.isConnected());
                telemetry.update();
                sleep(50);
            }
            if (isStopRequested()) return;
            if (!START_POSE_CONFIRMED || !Double.isFinite(START_X) || !Double.isFinite(START_Y)
                    || !Double.isFinite(START_HEADING_DEGREES)) {
                telemetry.addLine("ERROR: Set and confirm a finite measured start pose; restart the OpMode.");
                telemetry.update();
                return;
            }

            follower.setPose(new Pose(START_X, START_Y, Math.toRadians(START_HEADING_DEGREES)));
            LimelightPoseEstimator estimator = new LimelightPoseEstimator(HISTORY_MILLIS, MAX_PREDICTION_MILLIS);
            long lastSampleTime = Long.MIN_VALUE;
            while (opModeIsActive()) {
                follower.update();
                Pose odometry = follower.pose();
                double turretDegrees = turret.getTurretAngleFromMotor();
                long sampleTime = System.currentTimeMillis();
                if (sampleTime < lastSampleTime) {
                    telemetry.addLine("ERROR: Control Hub clock moved backwards; restart the test.");
                    telemetry.update();
                    break;
                }
                if (sampleTime > lastSampleTime) {
                    estimator.addSample(sampleTime, odometry, turretDegrees);
                    lastSampleTime = sampleTime;
                }

                LLResult result = limelight.getLatestResult();
                Estimate estimate = estimator.estimate(result);
                boolean driveEnabled = gamepad1.left_bumper;
                follower.manual(ManualDrive.fieldCentric(
                        driveEnabled ? driver.getLeftX() * DRIVE_SCALE : 0,
                        driveEnabled ? driver.getLeftY() * DRIVE_SCALE : 0,
                        driveEnabled ? -driver.getRightX() * DRIVE_SCALE : 0,
                        odometry.heading()));

                telemetry.addData("Drive", driveEnabled ? "Enabled (25%)" : "Hold left bumper");
                telemetry.addData("Limelight connected", limelight.isConnected());
                telemetry.addData("Turret angle (deg)", "%.1f", turretDegrees);
                telemetry.addData("Odometry", "x %.2f, y %.2f in, heading %.1f deg",
                        odometry.x(), odometry.y(), Math.toDegrees(odometry.heading()));
                telemetry.addData("Localization status", estimate.status);
                telemetry.addData("Tags used", estimate.tagsUsed);
                if (estimate.status == Status.OK) {
                    Pose vision = estimate.pose;
                    telemetry.addData("Vision pose", "x %.2f, y %.2f in, heading %.1f deg",
                            vision.x(), vision.y(), Math.toDegrees(vision.heading()));
                    telemetry.addData("Vision - odometry", "dx %.2f, dy %.2f in, heading %.1f deg",
                            vision.x() - odometry.x(), vision.y() - odometry.y(),
                            AngleUnit.normalizeDegrees(Math.toDegrees(vision.heading() - odometry.heading())));
                    telemetry.addData("Prediction after sample", "%d ms", estimate.poseTimestampMillis - sampleTime);
                }
                if (result != null && result.isValid()) {
                    telemetry.addData("Camera latency", "%.1f ms", result.getCaptureLatency() + result.getTargetingLatency());
                    telemetry.addData("Result age", "%d ms", result.getStaleness());
                    if (result.getFiducialResults() != null) {
                        for (FiducialResult tag : result.getFiducialResults()) {
                            if (tag == null) continue;
                            Pose3D observed = tag.getTargetPoseCameraSpace();
                            if (observed != null && observed.getPosition() != null) {
                                Position p = observed.getPosition().toUnit(DistanceUnit.METER);
                                telemetry.addData("Tag " + tag.getFiducialId(),
                                        "camera x %.2f, y %.2f, z %.2f m", p.x, p.y, p.z);
                            } else {
                                telemetry.addData("Tag " + tag.getFiducialId(), "No camera pose");
                            }
                        }
                    }
                } else {
                    telemetry.addLine("No valid Limelight result.");
                }
                telemetry.update();
                idle();
            }
        } finally {
            if (follower != null) follower.stop();
            limelight.stop();
        }
    }
}
