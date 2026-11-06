package com.metalbenders.samples;

import static com.metalbenders.util.Localization.localizeRobotWithMultiTags;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.metalbenders.hardware.AS5600;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@Disabled
@TeleOp(name = "Limelight Localization", group="sample")
public class LimelightLocalization  extends LinearOpMode {

    private Limelight3A limelight;

    private double turretMotorEncoderZero = 0;
    private AS5600 turretEncoder;
    private DcMotorEx turretMotor;
    public double TURRET_TICKS_PER_DEGREE = ( 1425.1 / 360 ) * 1.872340426;

    @Override
    public void runOpMode() {
        initialize();
        waitForStart();
        while (opModeIsActive()) {
            LLResult llResult = limelight.getLatestResult();
            Pose3D currentPose = localizeRobotWithMultiTags(llResult.getFiducialResults(), getTurretAngleFromMotor());
            double robotHeading = currentPose.getOrientation().getYaw();
            telemetry.addData("X Position", currentPose.getPosition().x);
            telemetry.addData("Y Position", currentPose.getPosition().y);
            telemetry.addData("Robot Heading", robotHeading);
            telemetry.addData("Turret Heading", robotHeading + getTurretAngleFromMotor());
            telemetry.update();

        }
        limelight.stop();
    }

    private void initialize() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();
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
