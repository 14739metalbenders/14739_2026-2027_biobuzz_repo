package com.metalbenders.samples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.metalbenders.hardware.AS5600;

//@Disabled
@TeleOp(name="AS5600 i2c Test", group="Sensors")
public class AS5600Test extends LinearOpMode {

    private double turretMotorEncoderZero = 0;
    private AS5600 turretEncoder;
    private DcMotorEx turretMotor;
    public double TURRET_TICKS_PER_DEGREE = ( 1425.1 / 360 ) * 1.872340426;

    @Override
    public void runOpMode() {

        long controlLoopStart = 0;
        controlLoopStart = System.currentTimeMillis();

        initialize();

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.setMsTransmissionInterval(50);

        telemetry.addData("Init Time", "%d ms", (System.currentTimeMillis() - controlLoopStart));
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            controlLoopStart = System.currentTimeMillis();

            telemetry.addData("Encoder Raw Angle (deg)", getTurretEncoderFixed());
            telemetry.addData("Encoder Robot Turret Angle (deg)", getTurretAngleFromEncoder());
            telemetry.addData("Motor Turret Angle (deg)", getTurretAngleFromMotor());
            telemetry.addData("LoopTime", "%d ms", (System.currentTimeMillis() - controlLoopStart));
            telemetry.update();
            sleep(20);

        }

    }

    private void initialize() {
        turretMotor = hardwareMap.get(DcMotorEx .class, "turretMotor");
        turretEncoder = hardwareMap.get(AS5600.class, "turretEncoder");
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        setTurretEncoderToMotorEncoderOffset();
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
        return AngleUnit.normalizeDegrees((currentTurretMotorEncoderValue - turretMotorEncoderZero) / TURRET_TICKS_PER_DEGREE);
    }

    private double getTurretAngleFromEncoder() {
        return AngleUnit.normalizeDegrees((getTurretEncoderFixed() / 1.872340426));
    }

}