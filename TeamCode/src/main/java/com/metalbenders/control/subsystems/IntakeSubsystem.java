package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.IntakeState.SHOOT;
import static com.metalbenders.constants.Constants.IntakeState.STOP;

public class IntakeSubsystem extends SubsystemBase {

    private final DcMotorEx intakeMotor;
    private final DcMotorEx intakeMotor2;
    private final Telemetry telemetry;
    private Constants.IntakeState intakeState = STOP;

    public IntakeSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        this.intakeMotor2 = hardwareMap.get(DcMotorEx.class, "intakeMotor2");
        this.intakeMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        this.intakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.intakeMotor2.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        this.intakeMotor2.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    }

    public void setIntakeState(Constants.IntakeState intakeState) {
        if (this.intakeState != intakeState) {
            this.intakeState = intakeState;
            intakeMotor.setVelocity(this.intakeState.getTargetVelocity());
            intakeMotor2.setVelocity(this.intakeState.getTargetVelocity() + 600);
        }
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Intake State", intakeState);
        telemetry.addData("Intake Velocity", intakeMotor.getVelocity());
        telemetry.addLine();
    }

    public void teardown() {
        setIntakeState(STOP);
    }
}
