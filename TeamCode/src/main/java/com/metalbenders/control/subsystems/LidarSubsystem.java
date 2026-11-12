package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.metalbenders.hardware.StudicaLidar;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Arrays;

public class LidarSubsystem extends SubsystemBase {

    private final StudicaLidar studicaLidar;
    private final Telemetry telemetry;
    private Constants.LidarState state;

    public LidarSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.studicaLidar = new StudicaLidar();
        this.telemetry = telemetry;
        studicaLidar.init(hardwareMap);
        studicaLidar.start();
        setState();
    }

    public double[] getDistanceInDegreeRange(int startDegreeRange, int endDegreeRange) {
        return Arrays.copyOfRange(studicaLidar.getRanges(true), startDegreeRange, endDegreeRange);
    }

    public double getDistanceAtDegree(int degree) {
        return studicaLidar.getDistanceAt(degree, true);
    }

    @Override
    public void periodic() {
        setState();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Lidar State", state);
        telemetry.addLine();
    }

    public void teardown() {
        studicaLidar.stop();
    }

    public void setState() {
        if (!studicaLidar.isConnected()) {
            state = Constants.LidarState.FAILED;
        } else {
            state = Constants.LidarState.CONNECTED;
        }
    }

    public boolean isHealthy() {
        return state == Constants.LidarState.CONNECTED;
    }
}
