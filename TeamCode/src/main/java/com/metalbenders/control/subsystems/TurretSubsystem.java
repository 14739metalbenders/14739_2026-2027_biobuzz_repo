package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;

public class TurretSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
//    private final DcMotorEx turretMotor;
    private final DcMotorEx launcher;
    private final Servo launchGateServo;
    private double targetLaunchVelocity;
    private double actualLaunchVelocity;
    //TODO: Tune PID values for launcher velocity
    private LaunchGateState launchGateState;
    private static final PIDFCoefficients LAUNCHER_VELOCITY_PID = new PIDFCoefficients(150, 0, 0, 3);

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
//        this.turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        this.launchGateServo = hardwareMap.get(Servo.class, "launchServo");
        this.launchGateState = LaunchGateState.CLOSED;
        this.launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.launcher.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                LAUNCHER_VELOCITY_PID.p, LAUNCHER_VELOCITY_PID.i, LAUNCHER_VELOCITY_PID.d, LAUNCHER_VELOCITY_PID.f));
        //TODO: This line will need to be removed and replaced with setting the velocity from the vision system.
        setTargetLaunchVelocity(1300);
    }

    public void setTargetLaunchVelocity(double velocity) {
        launcher.setVelocity(velocity);
        this.targetLaunchVelocity = velocity;
    }

    public boolean atLaunchVelocity() {
        return Math.abs(targetLaunchVelocity - actualLaunchVelocity) < 50;
    }

    public void closeLaunchGate() {
        launchGateState = LaunchGateState.CLOSED;
        launchGateServo.setPosition(launchGateState.getValue());
    }

    public void openLaunchGate() {
        launchGateState = LaunchGateState.OPEN;
        launchGateServo.setPosition(launchGateState.getValue());
    }

    @Override
    public void periodic() {
        actualLaunchVelocity = launcher.getVelocity();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target Velocity", targetLaunchVelocity);
        telemetry.addData("Actual Velocity", actualLaunchVelocity);
        telemetry.addData("Launch Gate Position", launchGateServo.getPosition());
        telemetry.addLine();
    }

    private enum LaunchGateState {
        OPEN(0.3),
        CLOSED(0.7);

        private final double value;

        LaunchGateState(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }
}
