package com.metalbenders.samples;

import static com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection.LEFT;
import static com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection.UP;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.ImuOrientationOnRobot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import com.metalbenders.hardware.StudicaLidar;

import java.util.List;

//@Disabled
@TeleOp(name = "DriveTest", group="sample")
public class DriveTest extends LinearOpMode {

    private DcMotorEx rightRearMotor;
    private DcMotorEx leftRearMotor;
    private DcMotorEx rightFrontMotor;
    private DcMotorEx leftFrontMotor;
    private IMU imu;
    private StudicaLidar studicaLidar = new StudicaLidar();

    private void setupHardware() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        rightFrontMotor = hardwareMap.get(DcMotorEx.class, "RFmotor");
        leftFrontMotor = hardwareMap.get(DcMotorEx.class, "LFmotor");
        rightRearMotor = hardwareMap.get(DcMotorEx.class, "RRmotor");
        leftRearMotor = hardwareMap.get(DcMotorEx.class, "LRmotor");
        imu = hardwareMap.get(IMU.class, "imu");
        studicaLidar.init(hardwareMap);
        if (!studicaLidar.isConnected()) {
            telemetry.addData("ERROR", "Lidar Not Found! Verify USB connections.");
            telemetry.update();
            return;
        }

        telemetry.addData("Status", "Lidar Connected! Ready.");
        telemetry.update();
        initializeHardware();
        initializeIMU();
    }

    @Override
    public void runOpMode() {
        setupHardware();
        waitForStart();
        studicaLidar.start();
        resetRuntime();
        while (opModeIsActive()) {
            double[] latestRanges = studicaLidar.getRanges();
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x * 0.4;
            double leftFrontPower = 0;
            double leftRearPower = 0;
            double rightFrontPower = 0;
            double rightRearPower = 0;
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            double botHeading = orientation.getYaw(AngleUnit.RADIANS);

            // Rotate the movement direction counter to the bot's rotation
            double rotX = lateral * Math.cos(-botHeading) - axial * Math.sin(-botHeading);
            double rotY = lateral * Math.sin(-botHeading) + axial * Math.cos(-botHeading);

            // Get commanded direction relative to bot
            double movementAngle = Math.floor(Math.toDegrees(getMovementDirectionRelativeToRobot(lateral, axial, botHeading))) - 90;
            movementAngle = (movementAngle % 360 + 360) % 360;
            // Flip rotation
            movementAngle = (360 - movementAngle) % 360;
            double lidarDistance = latestRanges[(int) movementAngle];

            if (lidarDistance > 300) {

                //calculate power
                double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(yaw), 1);
                leftFrontPower = (rotY + rotX + yaw) / denominator;
                leftRearPower = (rotY - rotX + yaw) / denominator;
                rightFrontPower = (rotY - rotX - yaw) / denominator;
                rightRearPower = (rotY + rotX - yaw) / denominator;

                // Normalize the values so no wheel power exceeds 100%
                // This ensures that the robot maintains the desired motion.
                double max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
                max = Math.max(max, Math.abs(leftRearPower));
                max = Math.max(max, Math.abs(rightRearPower));

                if (max > 1.0) {
                    leftFrontPower /= max;
                    rightFrontPower /= max;
                    leftRearPower /= max;
                    rightRearPower /= max;
                }
            }

            leftFrontMotor.setPower(leftFrontPower*1.00);
            rightFrontMotor.setPower(rightFrontPower*1.00);
            leftRearMotor.setPower(leftRearPower*1.00);
            rightRearMotor.setPower(rightRearPower*1.00);

            telemetry.addData("Relative Bot Direction","%.2f", movementAngle);
            telemetry.addData("Distance in direction of travel", lidarDistance > 0 ? String.format("%.1f mm", lidarDistance) : "Scanning...");
            telemetry.addData("Motor (Left Front)", "%.2f", leftFrontPower);
            telemetry.addData("Motor (Right Front)", "%.2f", rightFrontPower);
            telemetry.addData("Motor (Left Rear)","%.2f", leftRearPower);
            telemetry.addData("Motor (Right Rear)", "%.2f", leftRearPower);
            telemetry.addData("X", lateral);
            telemetry.addData("Y", axial);
            telemetry.update();
            //resetIMU();
        }
        studicaLidar.stop();
    }

    private void resetIMU() {
        if (gamepad1.dpad_up) {
            imu.resetYaw();
        }
    }

    private void initializeIMU() {
        ImuOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(UP, LEFT);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();
        sleep(1000);
    }

    private void initializeHardware(){
        for(DcMotor motor : List.of(rightFrontMotor, rightRearMotor)) {
            motor.setMode(RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(BRAKE);
            motor.setDirection(DcMotorSimple.Direction.REVERSE);
        }
        for(DcMotor motor : List.of(leftFrontMotor, leftRearMotor)) {
            motor.setMode(RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(BRAKE);
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
        }
    }

    public double getMovementDirectionRelativeToRobot(double stickX, double stickY, double robotHeading) {
        if (Math.hypot(stickX, stickY) < 0.05) {
            return 0.0;
        }

        double cosHeading = Math.cos(robotHeading);
        double sinHeading = Math.sin(robotHeading);

        double robotX = stickX * cosHeading + stickY * sinHeading;   // Robot-centric strafe (X)
        double robotY = -stickX * sinHeading + stickY * cosHeading;  // Robot-centric forward (Y)

        return Math.atan2(robotY, robotX);
    }
}
