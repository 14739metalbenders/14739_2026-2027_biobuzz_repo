package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.hardware.AS5600;
import com.metalbenders.util.TurretController.TurretRotateCalc;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import static com.metalbenders.util.TurretController.calculateTurretLead;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx turretMotor;
    private final AS5600 turretEncoder;
    public double TURRET_TICKS_PER_DEGREE = (1425.1 / 360) * 1.872340426;
    private double turretMotorEncoderZero = 0;
    private double shotLeadAngle = 0;
    private double shotTimeOfFLight = 0;
    private double targetBearing = 0;
    private double turretPower = 0;
    private double turretAngle = 0;


    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        //turret motor setup
        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.turretMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        this.turretMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.turretMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        this.turretEncoder = hardwareMap.get(AS5600.class, "turretEncoder");
        setTurretEncoderToMotorEncoderOffset();
    }

    public void updateTurretPosition(
            Twist currentTwist,
            double aimError,
            double launcherRPM) {
        setTurretPower(currentTwist, aimError, launcherRPM);
    }

    private double getTurretEncoderFixed() {
        return ((turretEncoder.getAngleDeg(true) * 2) - 196.8);
    }

    private void setTurretEncoderToMotorEncoderOffset() {
        long currentTurretMotorEncoderValue = turretMotor.getCurrentPosition();
        turretMotorEncoderZero = currentTurretMotorEncoderValue - (long) (getTurretAngleFromEncoder() * TURRET_TICKS_PER_DEGREE);
    }

    private double getTurretAngleFromMotor() {
        long currentTurretMotorEncoderValue = turretMotor.getCurrentPosition();
        turretAngle = AngleUnit.normalizeDegrees((currentTurretMotorEncoderValue - turretMotorEncoderZero) / TURRET_TICKS_PER_DEGREE);
        return turretAngle;
    }

    private double getTurretAngleFromEncoder() {
        return AngleUnit.normalizeDegrees((getTurretEncoderFixed() / 1.872340426));
    }

    private void setTurretPower(Twist currentTwist, double aimError, double launcherRPM) {
        TurretRotateCalc calculateTurretLead =
                calculateTurretLead(currentTwist, aimError, getTurretAngleFromMotor(), launcherRPM, 15);
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
