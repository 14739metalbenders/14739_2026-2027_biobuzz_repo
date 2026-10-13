package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
//    private final DcMotorEx turretMotor;
    private final DcMotorEx launcher;
    private double targetVelocity;
    private final PIDController launcherPIDController = new PIDController(0,0,0);

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
//        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        this.launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void setLauncherVelocity(double velocity) {
        launcher.setVelocity(velocity);
        this.targetVelocity = velocity;
    }

    @Override
    public void periodic() {
        double actualVelocity = launcher.getVelocity();
        launcher.setVelocity(launcherPIDController.calculate(actualVelocity, targetVelocity));
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target Velocity", targetVelocity);
        telemetry.addData("Actual Velocity", actualVelocity);
        telemetry.addLine();
    }
}
