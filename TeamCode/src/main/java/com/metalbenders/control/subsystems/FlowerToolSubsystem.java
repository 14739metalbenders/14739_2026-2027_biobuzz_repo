package com.metalbenders.control.subsystems;

import static com.metalbenders.constants.Constants.FlowerToolState.UP;

import com.arcrobotics.ftclib.command.Subsystem;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class FlowerToolSubsystem implements Subsystem {

    private final Servo flowerToolServo;
    private final Telemetry telemetry;
    private Constants.FlowerToolState flowerToolState = UP;

    public FlowerToolSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.flowerToolServo = hardwareMap.get(Servo.class, "flowerServo");
        flowerToolServo.setPosition(UP.getPosition());
    }

    public void setFlowerToolState(Constants.FlowerToolState flowerToolState) {
        if (this.flowerToolState != flowerToolState) {
            this.flowerToolState = flowerToolState;
            flowerToolServo.setPosition(flowerToolState.getPosition());
        }
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Flower Tool State", flowerToolState);
        telemetry.addLine();
    }

    public void teardown() {
        setFlowerToolState(UP);
    }
}
