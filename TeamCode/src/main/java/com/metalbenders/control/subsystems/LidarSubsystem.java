package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.Subsystem;
import com.metalbenders.constants.Constants;
import com.metalbenders.hardware.StudicaLidar;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Arrays;

public class LidarSubsystem implements Subsystem {

    private final StudicaLidar studicaLidar;
    private final Telemetry telemetry;
    private final Constants.LidarState state;
    public LidarSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.studicaLidar = new StudicaLidar();
        this.telemetry = telemetry;
        studicaLidar.init(hardwareMap);
        studicaLidar.start();
        if (!studicaLidar.isConnected()) {
            state = Constants.LidarState.FAILED;
        } else {
            state = Constants.LidarState.CONNECTED;
        }
    }

    public double[] getDistanceInDegreeRange(int startDegreeRange, int endDegreeRange) {
        return Arrays.copyOfRange(studicaLidar.getRanges(true), startDegreeRange, endDegreeRange);
    }

    public double getDistanceAtDegree(int degree) {
        return studicaLidar.getDistanceAt(degree, true);
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Lidar State", state);
        telemetry.addLine();
    }

    public void teardown() {
        studicaLidar.stop();
    }
}
