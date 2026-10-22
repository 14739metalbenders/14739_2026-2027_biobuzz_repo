package com.metalbenders.control.subsystems;

import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.LaunchVelocity;
import com.metalbenders.util.TimedEntity;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

import static com.metalbenders.constants.LaunchVelocity.getLaunchVelocityByDistance;
import static com.metalbenders.util.MathUtil.calculateAverageTx;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;

public class TurretSubsystem extends CloseableSubsystem {

    public enum LaunchGateState {
        OPEN,
        CLOSED,
        KICK
    }

    private final Telemetry telemetry;
    private final DcMotorEx turretMotor;
    private final DcMotorEx launcher;
    private final Servo launchGateServo;
    private LaunchVelocity targetLaunchVelocity = LaunchVelocity.DEFAULT;
    private double actualLaunchVelocity;
    private LaunchGateState launchGateState;
    private double targetHeadingDegrees = 0.0;
    private double turretHeadingDegrees = 0.0;
    private final Constants.AllianceColor allianceColor;
    private static final double MAX_TURRET_ROTATION_DEGREES = 120.0;
    private static final PIDFCoefficients LAUNCHER_VELOCITY_PID = new PIDFCoefficients(150, 0, 0, 3);
    private static final PIDFCoefficients TURRET_VELOCITY_PID = new PIDFCoefficients(23.25, 0.0, 0.8, 7.5);

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        this.launchGateServo = hardwareMap.get(Servo.class, "launchServo");
        // Force a known startup position/state so first shot command can open the gate.
        this.launchGateServo.setPosition(0.75);
        this.launchGateState = LaunchGateState.CLOSED;

        //launcher motor setup
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        this.launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.launcher.setMode(RUN_USING_ENCODER);
        this.launcher.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                LAUNCHER_VELOCITY_PID.p, LAUNCHER_VELOCITY_PID.i, LAUNCHER_VELOCITY_PID.d, LAUNCHER_VELOCITY_PID.f));

        //turret motor setup
        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                TURRET_VELOCITY_PID.p, TURRET_VELOCITY_PID.i, TURRET_VELOCITY_PID.d, TURRET_VELOCITY_PID.f));
    }

    public boolean atLaunchVelocity() {
        return targetLaunchVelocity.getTolerance() == null
                || Math.abs(targetLaunchVelocity.getVelocity() - actualLaunchVelocity) < targetLaunchVelocity.getTolerance();
    }

    public void closeLaunchGate() {
        if (launchGateState != LaunchGateState.CLOSED) {
            launchGateState = LaunchGateState.CLOSED;
            launchGateServo.setPosition(0.75);
        }
    }

    public void openLaunchGate() {
        if (launchGateState != LaunchGateState.OPEN) {
            launchGateState = LaunchGateState.OPEN;
            launchGateServo.setPosition(0.52);
        }
    }

    public void kickLaunchGate() {
        if (launchGateState != LaunchGateState.KICK) {
            launchGateState = LaunchGateState.KICK;
            launchGateServo.setPosition(0.08);
        }
    }

    public LaunchGateState getLaunchGateState() {
        return launchGateState;
    }

    public void updateLauncher(
            Constants.FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {
        setTargetLaunchVelocity(activeHiveCellFieldSide, currentPose, lastActiveSideDetections);
        setTargetHeadingDegrees(activeHiveCellFieldSide, currentPose, lastActiveSideDetections);
    }

    public void setTargetLaunchVelocity(
            Constants.FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {

        double distance;
        if (areDetectionsValid(lastActiveSideDetections)) {
            distance = calculateAverageTx(lastActiveSideDetections.getEntity());
        } else {
            distance = calculateDistanceFromOdometry(activeHiveCellFieldSide, currentPose);
        }
        this.targetLaunchVelocity = getLaunchVelocityByDistance(distance);
    }

    private double calculateDistanceFromOdometry(Constants.FieldSide activeHiveCellFieldSide, Pose currentPose) {
        double targetX;
        if (activeHiveCellFieldSide == Constants.FieldSide.SCORE_SIDE) {
            targetX = FieldLocation.SCORE_SIDE_HIVE_CELL.getPose(allianceColor).x();
        } else {
            targetX = FieldLocation.AUDIENCE_SIDE_HIVE_CELL.getPose(allianceColor).x();
        }
        return Math.abs(targetX - currentPose.x());
    }

    private void setTargetHeadingDegrees(
            Constants.FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {
        double targetHeadingDegrees;
        if (areDetectionsValid(lastActiveSideDetections)) {
            targetHeadingDegrees = calculateHeadingDegreesFromDetections(
                    lastActiveSideDetections.getEntity(), currentPose);
        } else {
            targetHeadingDegrees = getHeadingDegreesFromOdometry(activeHiveCellFieldSide, currentPose);
        }
        setHeadingDegrees(targetHeadingDegrees, currentPose);
    }

    private double calculateHeadingDegreesFromDetections(
            List<LLResultTypes.FiducialResult> fiducialResults, Pose currentPose) {
        return 0;
    }

    private double getHeadingDegreesFromOdometry(Constants.FieldSide activeHiveCellFieldSide, Pose currentPose) {
        double targetHeading;
        if (activeHiveCellFieldSide == Constants.FieldSide.SCORE_SIDE) {
            targetHeading = Math.toDegrees(Math.atan2(
                    FieldLocation.SCORE_SIDE_HIVE_CELL.getPose(allianceColor).y() - currentPose.y(),
                    FieldLocation.SCORE_SIDE_HIVE_CELL.getPose(allianceColor).x() - currentPose.x()));
        } else {
            targetHeading = Math.toDegrees(Math.atan2(
                    FieldLocation.AUDIENCE_SIDE_HIVE_CELL.getPose(allianceColor).y() - currentPose.y(),
                    FieldLocation.AUDIENCE_SIDE_HIVE_CELL.getPose(allianceColor).x() - currentPose.x()));
        }
        return targetHeading;
    }

    private void setHeadingDegrees(double targetHeadingDegrees, Pose currentPose) {
        this.targetHeadingDegrees = targetHeadingDegrees;
        double turretHeadingDegrees =  Math.toDegrees(currentPose.heading()) - targetHeadingDegrees;
        if (turretHeadingDegrees > MAX_TURRET_ROTATION_DEGREES) {
            turretHeadingDegrees = MAX_TURRET_ROTATION_DEGREES;
        } else if (turretHeadingDegrees < -MAX_TURRET_ROTATION_DEGREES) {
            turretHeadingDegrees = -MAX_TURRET_ROTATION_DEGREES;
        }
        this.turretHeadingDegrees = turretHeadingDegrees;
    }

    private boolean areDetectionsValid(TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {
        return lastActiveSideDetections != null
                && lastActiveSideDetections.getTime() < 500
                && lastActiveSideDetections.getEntity() != null
                && !lastActiveSideDetections.getEntity().isEmpty();
    }

    @Override
    public void periodic() {
        launcher.setVelocity(targetLaunchVelocity.getVelocity());
        actualLaunchVelocity = launcher.getVelocity();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target Velocity",
                targetLaunchVelocity.name() + " (" + targetLaunchVelocity.getVelocity() + " rpm)");
        telemetry.addData("Actual Velocity", actualLaunchVelocity);
        telemetry.addData("Launch Gate State", launchGateState);
        telemetry.addData("Target Heading (degrees)", targetHeadingDegrees);
        telemetry.addData("Turret Heading (degrees)", turretHeadingDegrees);
        telemetry.addLine();
    }

    @Override
    public void close(Constants.OpModeType opModeType) {
        closeLaunchGate();
    }
}
