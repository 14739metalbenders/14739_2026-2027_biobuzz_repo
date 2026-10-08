package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;

public abstract class BaseOpMode extends CommandOpMode {

    public abstract AllianceColor getAllianceColor();

    public abstract Pose getInitialPose();

    public void updateRuntime() {
        double totalSeconds = getRuntime();
        telemetry.addData(
                "Runtime",
                "%02.0f:%02.0f",
                totalSeconds / 60,
                totalSeconds % 60);
    }

    @Override
    public void run() {
        super.run();
        updateRuntime();
        telemetry.update();
    }
}
