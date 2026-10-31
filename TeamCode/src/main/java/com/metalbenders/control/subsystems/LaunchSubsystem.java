package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.constants.LaunchVelocity;
import com.metalbenders.util.TimedEntity;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

import static com.metalbenders.constants.Constants.*;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.LaunchVelocity.getLaunchVelocityByDistance;
import static com.metalbenders.util.MathUtil.calculateAverageTx;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;

public class LaunchSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx launcher;
    private final Servo launchGateServo;
    private LaunchVelocity targetLaunchVelocity = LaunchVelocity.IDLE;
    private double actualLaunchVelocity;
    private LaunchGateState launchGateState;
    private final AllianceColor allianceColor;

    public LaunchSubsystem(HardwareMap hardwareMap, Telemetry telemetry, AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        this.launchGateServo = hardwareMap.get(Servo.class, "launchServo");
        // Force a known startup position/state so first shot command can open the gate.
        this.launchGateState = CLOSED;
        this.launchGateServo.setPosition(CLOSED.getPosition());

        //launcher motor setup
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        this.launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.launcher.setMode(RUN_USING_ENCODER);
        this.launcher.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                LAUNCHER_VELOCITY_PID.p, LAUNCHER_VELOCITY_PID.i, LAUNCHER_VELOCITY_PID.d, LAUNCHER_VELOCITY_PID.f));
    }

    public void setLaunchGateState(LaunchGateState launchGateState) {
        this.launchGateState = launchGateState;
        launchGateServo.setPosition(launchGateState.getPosition());
    }

    public void updateLauncherVelocity(
            FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections,
            double distance) {
        setTargetLaunchVelocity(activeHiveCellFieldSide, currentPose, lastActiveSideDetections, distance);
    }

    public void setTargetLaunchVelocity(
            FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections,
            double distance) {

        //double distance;
        //if (areDetectionsValid(lastActiveSideDetections)) {
        //    distance = calculateAverageTx(lastActiveSideDetections.getEntity());
        //} else {
        //    distance = calculateDistanceFromOdometry(activeHiveCellFieldSide, currentPose);
        //}
        this.targetLaunchVelocity = getLaunchVelocityByDistance(distance * 39.3701);
    }

    private double calculateDistanceFromOdometry(FieldSide activeHiveCellFieldSide, Pose currentPose) {
        double targetX;
        if (activeHiveCellFieldSide == FieldSide.SCORE_SIDE) {
            targetX = FieldLocation.SCORE_SIDE_HIVE_CELL.getPose(allianceColor).x();
        } else {
            targetX = FieldLocation.AUDIENCE_SIDE_HIVE_CELL.getPose(allianceColor).x();
        }
        return Math.abs(targetX - currentPose.x());
    }

    private boolean areDetectionsValid(TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {
        return lastActiveSideDetections != null
                && lastActiveSideDetections.getTime() < 500
                && lastActiveSideDetections.getEntity() != null
                && !lastActiveSideDetections.getEntity().isEmpty();
    }

    public double getLauncherRPM() {
        // (Velocity from motor * 60 sec) / 1425.1 (PPR For goBilda 117 RPM Motor)
        return (launcher.getVelocity() * 60) / 1425.1;
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
        telemetry.addLine();
    }

    public void teardown() {
        setLaunchGateState(CLOSED);
    }
}
