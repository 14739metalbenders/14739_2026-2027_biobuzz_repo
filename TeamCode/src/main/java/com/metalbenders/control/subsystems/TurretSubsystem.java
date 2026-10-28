package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldLocation;
import com.metalbenders.util.TimedEntity;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

import static com.metalbenders.constants.Constants.*;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx turretMotor;
    private double targetHeadingDegrees = 0.0;
    private double turretHeadingDegrees = 0.0;
    private final Constants.AllianceColor allianceColor;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;

        //turret motor setup
        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                TURRET_VELOCITY_PID.p, TURRET_VELOCITY_PID.i, TURRET_VELOCITY_PID.d, TURRET_VELOCITY_PID.f));
    }

    public void updateTurretPosition(
            Constants.FieldSide activeHiveCellFieldSide,
            Pose currentPose,
            TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections) {
        setTargetHeadingDegrees(activeHiveCellFieldSide, currentPose, lastActiveSideDetections);
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
        FieldLocation hiveLocation = FieldLocation.getHiveCellLocation(activeHiveCellFieldSide);
        return Math.toDegrees(
                Math.atan2(
                        hiveLocation.getPose(allianceColor).y() - currentPose.y(),
                        hiveLocation.getPose(allianceColor).x() - currentPose.x()));
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
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target Heading (degrees)", targetHeadingDegrees);
        telemetry.addData("Turret Heading (degrees)", turretHeadingDegrees);
        telemetry.addLine();
    }

    public void teardown() {
        //TODO add any necessary teardown logic for the turret subsystem
    }
}
