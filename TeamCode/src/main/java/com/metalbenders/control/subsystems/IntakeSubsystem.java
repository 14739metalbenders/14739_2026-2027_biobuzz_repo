package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.util.TimedEntity;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class IntakeSubsystem extends SubsystemBase {

    private enum IntakeState {
        INTAKE,
        REVERSE_INTAKE,
        STOPPED
    }

    private final DcMotorEx intakeMotor;
    private final Telemetry telemetry;
    private IntakeState intakeState = IntakeState.STOPPED;

    public IntakeSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        intakeMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    public void intake() {
        intakeMotor.setPower(1);
        intakeState = IntakeState.INTAKE;
    }

    public void reverseIntake() {
        intakeMotor.setPower(-1);
        intakeState = IntakeState.REVERSE_INTAKE;
    }

    public void stop() {
        intakeMotor.setPower(0);
        intakeState = IntakeState.STOPPED;
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Intake State", intakeState);
        telemetry.addLine();
    }
}
