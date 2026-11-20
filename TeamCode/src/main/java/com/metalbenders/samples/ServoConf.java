package com.metalbenders.samples;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

//@Disabled
@TeleOp(name = "ServoConf", group = "Hardware")
@Config
public class ServoConf extends LinearOpMode {

    private Servo LH_Servo;
    private Servo RH_Servo;

    public double leftServoPos1 = 0.75;
    public double leftServoPos2 = 0.15;

    public double rightServoPos1 = 0.25;
    public double rightServoPos2 = 0.85;


    @Override
    public void runOpMode() {
        initialize();
        waitForStart();



        while (opModeIsActive()) {
            if (gamepad1.a) {
                LH_Servo.setPosition(leftServoPos1);
                RH_Servo.setPosition(rightServoPos1);
            } else {
                LH_Servo.setPosition(leftServoPos2);
                RH_Servo.setPosition(rightServoPos2);
            }
        }
    }

    private void initialize() {

        this.LH_Servo = hardwareMap.get(Servo.class, "flowerServoLH");
        this.RH_Servo = hardwareMap.get(Servo.class, "flowerServoRH");
    }
}
