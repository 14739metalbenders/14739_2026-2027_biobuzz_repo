package com.metalbenders.opmodes;

import com.metalbenders.constants.AllianceColor;
import com.metalbenders.subsystems.Subsystem;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public abstract class BaseOpMode extends LinearOpMode {

    abstract Subsystem[] getSubsystems();
    abstract AllianceColor getAllianceColor();

    @Override
    public void runOpMode() throws InterruptedException {
        for(Subsystem subsystem : getSubsystems()) {
            subsystem.init(hardwareMap);
        }
        waitForStart();
        resetRuntime();
        while(opModeIsActive()) {
            for(Subsystem subsystem : getSubsystems()) {
                subsystem.update(telemetry);
            }
            updateRuntime();
            telemetry.update();
        }
        for(Subsystem subsystem : getSubsystems()) {
            subsystem.stop();
        }
    }

    private void updateRuntime() {
        double totalSeconds = getRuntime();
        telemetry.addData(
                "Runtime",
                "%02.0f:%02.0f",
                totalSeconds / 60,
                totalSeconds % 60);
    }
}
