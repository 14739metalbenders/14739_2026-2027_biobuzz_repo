package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx turretMotor;
    private final DcMotorEx launchMotor;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.launchMotor = hardwareMap.get(DcMotorEx.class, "launchMotor");
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addLine();
    }
}
