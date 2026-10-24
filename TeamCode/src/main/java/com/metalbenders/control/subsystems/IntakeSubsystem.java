package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class IntakeSubsystem extends SubsystemBase {

    private final DcMotorEx intakeMotor;
    private final Telemetry telemetry;
    private Constants.IntakeState intakeState = Constants.IntakeState.STOP;

    public IntakeSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        this.intakeMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    public void setIntakeState(Constants.IntakeState intakeState) {
        if (this.intakeState != intakeState) {
            this.intakeState = intakeState;
            intakeMotor.setVelocity(this.intakeState.getTargetVelocity());
        }
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Intake State", intakeState);
        telemetry.addLine();
    }

    public void teardown() {
        setIntakeState(Constants.IntakeState.STOP);
    }
}
