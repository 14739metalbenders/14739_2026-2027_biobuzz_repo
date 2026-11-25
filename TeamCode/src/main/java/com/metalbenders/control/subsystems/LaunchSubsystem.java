package com.metalbenders.control.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.VelocityLookupTable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.CONVERT_METERS_TO_INCHES;
import static com.metalbenders.constants.Constants.LAUNCHER_VELOCITY_PID;
import static com.metalbenders.constants.Constants.LaunchGateState;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.OpModeType;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;

@Config
public class LaunchSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final DcMotorEx launcher;
    private final Servo launchGateServo;
    private final Servo deflectorServo;
    private double targetLaunchVelocity;
    private LaunchGateState launchGateState;
    private long shotSequence;
    public static double theVelocity = 0;
    public static double deflectorPosition = 0.0;

    public LaunchSubsystem(HardwareMap hardwareMap, Telemetry telemetry, OpModeType opModeType) {
        this.telemetry = telemetry;
        this.launchGateServo = hardwareMap.get(Servo.class, "launchServo");
        this.deflectorServo = hardwareMap.get(Servo.class, "deflectorServo");
        // Force a known startup position/state so first shot command can open the gate.
        this.launchGateState = CLOSED;
        this.launchGateServo.setPosition(CLOSED.getPosition());

        //launcher motor setup
        this.launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        this.launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.launcher.setMode(RUN_USING_ENCODER);
        this.launcher.setPIDFCoefficients(RUN_USING_ENCODER, new PIDFCoefficients(
                LAUNCHER_VELOCITY_PID.p, LAUNCHER_VELOCITY_PID.i, LAUNCHER_VELOCITY_PID.d, LAUNCHER_VELOCITY_PID.f));
        this.targetLaunchVelocity = VelocityLookupTable.getTargetVelocity(0.0);
    }

    public void setLaunchGateState(LaunchGateState launchGateState) {
        if (launchGateState == LaunchGateState.OPEN && this.launchGateState != LaunchGateState.OPEN) {
            shotSequence++;
        }
        this.launchGateState = launchGateState;
        launchGateServo.setPosition(launchGateState.getPosition());
    }

    public long getShotSequence() {
        return shotSequence;
    }

    public boolean isShooting() {
        return launchGateState != CLOSED;
    }

    public double getTargetVelocity() {
        return theVelocity > 0 ? theVelocity : targetLaunchVelocity;
    }

    public double getActualVelocity() {
        return launcher.getVelocity();
    }

    public void updateLauncherVelocity(double distance) {
        if (distance > 0) {
            targetLaunchVelocity = VelocityLookupTable.getTargetVelocity(distance * CONVERT_METERS_TO_INCHES);
        }
    }

    public double getLauncherRPM() {
        // (Velocity from motor * 60 sec) / 1425.1 (PPR For goBilda 117 RPM Motor)
        return (launcher.getVelocity() * 60) / 28;
    }

    @Override
    public void periodic() {
        launcher.setVelocity(getTargetVelocity());
        deflectorServo.setPosition(deflectorPosition);

        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Target Velocity", getTargetVelocity());
        telemetry.addData("Actual Velocity", launcher.getVelocity());
        telemetry.addData("Launch Gate State", launchGateState);
        telemetry.addData("Deflector Position", deflectorPosition);
        telemetry.addLine();
    }

    public void teardown() {
        setLaunchGateState(CLOSED);
    }
}
