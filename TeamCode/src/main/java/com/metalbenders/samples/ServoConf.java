package com.metalbenders.samples;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

//@Disabled
@TeleOp(name = "ServoConf", group="Hardware")
@Config
public class ServoConf extends LinearOpMode {

    private Servo angleServo;

    @Override
    public void runOpMode() {
        initialize();
        waitForStart();

        double positionGate = 0.85;
        double positionLaunch = 0.25;
        //double positionFinish = 0.6;

        while (opModeIsActive()) {
            if (gamepad1.a) {
                angleServo.setPosition(positionLaunch);
            //} else if (gamepad1.b) {
            //    angleServo.setPosition(positionFinish);
            } else {
                angleServo.setPosition(positionGate);
            }
        }
    }

    private void initialize() {
        this.angleServo = hardwareMap.get(Servo.class, "flowerServo");
    }
}
