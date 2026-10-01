package com.metalbenders.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public abstract class Subsystem {

    /**
     * Called once when the robot finishes initializing hardware maps.
     * Use this to configure hardware directions, initial positions, or run modes.
     */
    public abstract void init(HardwareMap hardwareMap);

    /**
     * Called repeatedly inside the active OpMode loop (typically 50-100+ times per second).
     * Use this for sensor reads, calculating PID values, or executing background updates.
     */
    public abstract void update(Telemetry telemetry);

    /**
     * Safely halts all motors and actuators inside the subsystem.
     * Essential for emergency stops or transitioning between states.
     */
    public abstract void stop();
}