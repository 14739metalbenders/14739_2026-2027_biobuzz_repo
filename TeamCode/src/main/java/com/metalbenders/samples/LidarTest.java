package com.metalbenders.samples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import com.metalbenders.hardware.StudicaLidar;

@TeleOp(name = "Studica T-Mini Lidar Test", group = "Sensors")
public class LidarTest extends LinearOpMode{

    @Override
    public void runOpMode() throws InterruptedException {

        long controlLoopStart = 0;
        controlLoopStart = System.currentTimeMillis();

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addData("Status", "Connecting Android USB System...");
        telemetry.update();

        StudicaLidar studicaLidar = new StudicaLidar();

        studicaLidar.init(hardwareMap);

        if (!studicaLidar.isConnected()) {
            telemetry.addData("ERROR", "Lidar Not Found! Verify USB connections.");
            telemetry.update();
            return;
        }

        telemetry.addData("Status", "Lidar Connected! Ready.");
        telemetry.addData("Init Time", "%d ms", (System.currentTimeMillis() - controlLoopStart));
        telemetry.update();

        waitForStart();
        studicaLidar.start();

        while (opModeIsActive()) {
            controlLoopStart = System.currentTimeMillis();

            double[] latestRanges = studicaLidar.getRanges(true);

            double deg0 = latestRanges[0];   // 0° (Straight Forward)
            double deg45 = latestRanges[45];
            double deg90 = latestRanges[90];  // 90° (Straight Right)
            double deg135 = latestRanges[135];
            double deg180  = latestRanges[180]; // 180° (Straight Back)
            double deg225 = latestRanges[225];
            double deg270  = latestRanges[270]; // 270° (Straight Left)
            double deg315 = latestRanges[315];

            telemetry.addLine("Studica T-Mini Lidar Output");
            telemetry.addData("Forward (0°)", deg0 > 0 ? String.format("%.1f mm", deg0) : "Scanning...");
            telemetry.addData("45°", deg45 > 0 ? String.format("%.1f mm", deg45) : "Scanning...");
            telemetry.addData("Right (90°)", deg90 > 0 ? String.format("%.1f mm", deg90) : "Scanning...");
            telemetry.addData("135°", deg135 > 0 ? String.format("%.1f mm", deg135) : "Scanning...");
            telemetry.addData("Rear (180°)", deg180 > 0  ? String.format("%.1f mm", deg180)  : "Scanning...");
            telemetry.addData("225°", deg225 > 0 ? String.format("%.1f mm", deg225) : "Scanning...");
            telemetry.addData("Left (270°)", deg270 > 0  ? String.format("%.1f mm", deg270)  : "Scanning...");
            telemetry.addData("315°", deg315 > 0 ? String.format("%.1f mm", deg315) : "Scanning...");
            telemetry.addData("Total Packets Parsed", studicaLidar.getPacketCount());
            telemetry.addData("LoopTime", "%d ms", (System.currentTimeMillis() - controlLoopStart));
            telemetry.update();

        }

        studicaLidar.stop();

    }

}
