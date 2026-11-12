package com.metalbenders.samples;

import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "TestIndicatorLight", group = "sample")
public class TestIndicatorLight extends LinearOpMode {

    private Servo lightingServo;
    private final Constants.IndicatorLedEnum[] colorCycle = Constants.IndicatorLedEnum.values();
    private int currentColorIndex = 0;
    private boolean lastDpadRight = false;
    private boolean lastDpadLeft = false;

    @Override
    public void runOpMode() {
        // Initialize the light as a standard Servo
        lightingServo = hardwareMap.get(Servo.class, "signalLed");

        // Set initial color during initialization
        lightingServo.setPosition(colorCycle[currentColorIndex].getLedValue());
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Current Color", colorCycle[currentColorIndex].name());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Advance one color on DPAD right press
            boolean dpadRightPressed = gamepad1.dpad_right;
            if (dpadRightPressed && !lastDpadRight) {
                currentColorIndex = (currentColorIndex + 1) % colorCycle.length;
                lightingServo.setPosition(colorCycle[currentColorIndex].getLedValue());
            }
            lastDpadRight = dpadRightPressed;

            // Go back one color on DPAD left press
            boolean dpadLeftPressed = gamepad1.dpad_left;
            if (dpadLeftPressed && !lastDpadLeft) {
                currentColorIndex = (currentColorIndex - 1 + colorCycle.length) % colorCycle.length;
                lightingServo.setPosition(colorCycle[currentColorIndex].getLedValue());
            }
            lastDpadLeft = dpadLeftPressed;

            telemetry.addData("Current Color", colorCycle[currentColorIndex].name());
            telemetry.addData("Light Position", lightingServo.getPosition());
            telemetry.update();
        }
    }
}
