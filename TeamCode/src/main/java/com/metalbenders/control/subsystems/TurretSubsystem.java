package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
//    private final DcMotorEx turretMotor;
    private final DcMotorEx launcher;
    private double power;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
//        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
    }

    public void setLauncherPower(double power) {
        launcher.setPower(power);
        this.power = power;
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Power", power);
        telemetry.addLine();
    }
}
