package com.metalbenders.control.subsystems;


import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants.OpModeType;
import com.metalbenders.util.LimelightPoseEstimator;
import com.metalbenders.util.LimelightPoseEstimator.Estimate;
import com.metalbenders.util.LimelightPoseEstimator.Status;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Locale;

import static com.metalbenders.constants.Constants.AUTO_SPEED_MULTIPLIER;
import static com.pedropathing.api.Paths.line;

public class DriveSubsystem extends SubsystemBase {
    private static final long LOCALIZATION_HISTORY_MILLIS = 1000;
    private static final long LOCALIZATION_MAX_PREDICTION_MILLIS = 100;
    private static final double MAX_VISION_HEADING_ERROR_DEGREES = 15.0;

    private final Follower follower;
    private final Telemetry telemetry;
    private final LimelightSubsystem limelightSubsystem;
    private final TurretSubsystem turretSubsystem;
    private final LimelightPoseEstimator poseEstimator;
    private long lastSampleTime = Long.MIN_VALUE;
    private double lastAppliedFrameTimestamp = Double.NaN;

    public DriveSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Pose initialPose,
                          OpModeType opModeType, LimelightSubsystem limelightSubsystem,
                          TurretSubsystem turretSubsystem) {
        this.telemetry = telemetry;
        this.limelightSubsystem = limelightSubsystem;
        this.turretSubsystem = turretSubsystem;
        this.poseEstimator = opModeType == OpModeType.TELEOP
                ? new LimelightPoseEstimator(LOCALIZATION_HISTORY_MILLIS, LOCALIZATION_MAX_PREDICTION_MILLIS)
                : null;
        this.follower = Constants.create(hardwareMap);
        this.follower.setPose(initialPose);
    }

    public void drive(double forward, double strafe, double heading) {
        follower.manual(
                ManualDrive.fieldCentric(forward, strafe, heading, follower.pose().heading()));
    }

    public void driveToPosition(Pose pose, Double maxSpeed) {
        if (maxSpeed == null) {
            maxSpeed = 1.0;
        }
        follower.follow(
                line(follower.pose(), pose).constant(pose.heading())
                        .with(Constants.foresightConfig.maxPathSpeed.at(maxSpeed * AUTO_SPEED_MULTIPLIER)));
    }

    public Pose getCurrentPose() {
        return follower.pose();
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
        if (poseEstimator != null) {
            poseEstimator.clearOdometryHistory();
        }
    }

    public Twist getCurrentTwist() {
        return follower.twist();
    }

    public boolean isBusy() {
        return follower.isBusy();
    }

    public void stop() {
        follower.stop();
    }

    @Override
    public void periodic() {
        follower.update();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        if (poseEstimator != null) {
            updateLocalization();
        }
        telemetry.addData("Pose",
                String.format(Locale.ENGLISH, "x: %.2f, y: %.2f, heading: %.2f",
                        follower.pose().x(), follower.pose().y(), Math.toDegrees(follower.pose().heading())));
        telemetry.addData("Driving", follower.isBusy() ? "Yes" : "No");
        //telemetry.addData("Xvector", follower.twist().vx);
        //telemetry.addData("Yvector", follower.twist().vy);
        //telemetry.addData("Omega", follower.twist().omega);
        telemetry.addLine();
    }

    private void updateLocalization() {
        Pose odometry = follower.pose();
        double turretDegrees = turretSubsystem.getTurretAngleFromMotor();
        long sampleTime = System.currentTimeMillis();
        if (sampleTime > lastSampleTime) {
            poseEstimator.addSample(sampleTime, odometry, turretDegrees);
            lastSampleTime = sampleTime;
        }

        LLResult result = limelightSubsystem.getLatestResult();
        if (result != null && result.getTimestamp() == lastAppliedFrameTimestamp) {
            telemetry.addData("Localization Status", "Frame already applied");
            telemetry.addData("Localization Correction", "Not applied");
            return;
        }

        Estimate estimate = poseEstimator.estimate(result);
        telemetry.addData("Localization Status", estimate.status);
        telemetry.addData("Localization Tags", estimate.tagsUsed);
        if (estimate.status != Status.OK) {
            telemetry.addData("Localization Correction", "Not applied");
            return;
        }

        double headingErrorDegrees = visionHeadingErrorDegrees(odometry, estimate.pose);
        telemetry.addData("Localization Vision Heading (deg)", "%.1f",
                Math.toDegrees(estimate.pose.heading()));
        telemetry.addData("Localization Heading Error (deg)", "%.1f", headingErrorDegrees);
        // Vision uses its heading to calculate X/Y, so a large disagreement also invalidates position.
        if (!isVisionHeadingConsistent(headingErrorDegrees)) {
            telemetry.addData("Localization Correction", "Rejected: heading disagreement");
            return;
        }

        Pose correctedPose = new Pose(estimate.pose.x(), estimate.pose.y(), odometry.heading());
        setPose(correctedPose);
        lastAppliedFrameTimestamp = result.getTimestamp();
        telemetry.addData("Localization Correction", "Applied X/Y only");
        telemetry.addData("Localization Pose", "x %.2f, y %.2f, heading %.1f deg",
                correctedPose.x(), correctedPose.y(), Math.toDegrees(correctedPose.heading()));
    }

    static double visionHeadingErrorDegrees(Pose odometry, Pose vision) {
        return AngleUnit.normalizeDegrees(Math.toDegrees(vision.heading() - odometry.heading()));
    }

    static boolean isVisionHeadingConsistent(double headingErrorDegrees) {
        return Math.abs(headingErrorDegrees) <= MAX_VISION_HEADING_ERROR_DEGREES;
    }

    public void teardown() {
        stop();
    }
}
