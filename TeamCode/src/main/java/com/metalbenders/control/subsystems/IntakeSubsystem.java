package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeSubsystem extends SubsystemBase {

    private final DcMotorEx intakeMotor;

    public IntakeSubsystem(HardwareMap hardwareMap) {
         intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
    }

    public void intake() {
        intakeMotor.setPower(1);
    }

    public void reverseIntake() {
        intakeMotor.setPower(-1);
    }

    public void stop() {
        intakeMotor.setPower(0);
    }
}
