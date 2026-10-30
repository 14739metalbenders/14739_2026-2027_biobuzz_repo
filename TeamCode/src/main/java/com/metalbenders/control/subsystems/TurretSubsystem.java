package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.hardware.AS5600;
import com.metalbenders.util.TimedEntity;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import com.metalbenders.util.TurretController.TurretRotateCalc;
import static com.metalbenders.util.TurretController.calculateTurretLead;

import java.util.List;

import static com.metalbenders.constants.Constants.*;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx turretMotor;
    private final AS5600 turretEncoder;
    private double targetHeadingDegrees = 0.0;
    private double turretHeadingDegrees = 0.0;
    private final Constants.AllianceColor allianceColor;
    private double turretMotorEncoderZero = 0;
    public double TURRET_TICKS_PER_DEGREE = ( 1425.1 / 360 ) * 1.872340426;

    private double shotLeadAngle = 0;
    private double shotTimeOfFLight = 0;
    private double targetBearing = 0;
    private double turretPower = 0;
    private double turretAngle = 0;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;

        //turret motor setup
        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.turretMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        this.turretMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.turretMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        //this.turretMotor.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
        //        TURRET_VELOCITY_PID.p, TURRET_VELOCITY_PID.i, TURRET_VELOCITY_PID.d, TURRET_VELOCITY_PID.f));
        this.turretEncoder = hardwareMap.get(AS5600.class, "turretEncoder");
        setTurretEncoderToMotorEncoderOffset();
    }

    public void updateTurretPosition(
            Constants.FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            Twist currentTwist,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections,
            double aimError,
            double launcherRPM) {
        setTargetHeadingDegrees(activeHiveCellFieldSide, currentPose, lastActiveSideDetections);
        turretPower(currentTwist, aimError, launcherRPM);
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

    private double getTurretEncoderFixed() {
        return ((turretEncoder.getAngleDeg(true) * 2) - 196.8);
    }

    private void setTurretEncoderToMotorEncoderOffset() {
        long currentTurretMotorEncoderValue = turretMotor.getCurrentPosition();
        turretMotorEncoderZero = currentTurretMotorEncoderValue - (long)(getTurretAngleFromEncoder() * TURRET_TICKS_PER_DEGREE);
    }

    private double getTurretAngleFromMotor() {
        long currentTurretMotorEncoderValue = turretMotor.getCurrentPosition();
        turretAngle = AngleUnit.normalizeDegrees((currentTurretMotorEncoderValue - turretMotorEncoderZero) / TURRET_TICKS_PER_DEGREE);
        return turretAngle;
    }

    private double getTurretAngleFromEncoder() {
        return AngleUnit.normalizeDegrees((getTurretEncoderFixed() / 1.872340426));
    }

    private void turretPower(Twist currentTwist, double aimError, double launcherRPM) {
        TurretRotateCalc calculateTurretLead = calculateTurretLead(currentTwist, aimError, getTurretAngleFromMotor(), launcherRPM, 15);
        shotLeadAngle = calculateTurretLead.getShotLeadAngle();
        shotTimeOfFLight = calculateTurretLead.getTimeOfFlight();
        targetBearing = calculateTurretLead.getTargetBearing();
        turretPower = calculateTurretLead.getTurretPower();
        setTurretPower(calculateTurretLead.getTurretPower());
    }

    private void setTurretPower(double turretPower) {
        this.turretMotor.setPower(turretPower);
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        //telemetry.addData("Target Heading (degrees)", targetHeadingDegrees);
        //telemetry.addData("Turret Heading (degrees)", turretHeadingDegrees);
        telemetry.addData("Turret Power", turretPower);
        telemetry.addData("Turret Angle (deg)", turretAngle);
        telemetry.addData("Target Lead (deg)", shotLeadAngle);
        telemetry.addData("Target Bearing (deg)", targetBearing);
        telemetry.addData("Shot ToF (s)", shotTimeOfFLight);
        telemetry.addLine();
    }

    public void teardown() {
        //TODO add any necessary teardown logic for the turret subsystem
    }
}
