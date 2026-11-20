package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.FlowerToolState.UP;

public class FlowerToolSubsystem extends SubsystemBase {

    private final Servo flowerToolServoLH;
    private final Servo flowerToolServoRH;
    private final Telemetry telemetry;
    private Constants.FlowerToolState flowerToolState = UP;

    public FlowerToolSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.flowerToolServoLH = hardwareMap.get(Servo.class, "flowerServoLH");
        this.flowerToolServoRH = hardwareMap.get(Servo.class, "flowerServoRH");
        flowerToolServoLH.setPosition(UP.getLeftPosition());
        flowerToolServoRH.setPosition(UP.getRightPosition());
    }

    public void setFlowerToolState(Constants.FlowerToolState flowerToolState) {
        if (this.flowerToolState != flowerToolState) {
            this.flowerToolState = flowerToolState;
            flowerToolServoLH.setPosition(flowerToolState.getLeftPosition());
            flowerToolServoRH.setPosition(flowerToolState.getRightPosition());
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
