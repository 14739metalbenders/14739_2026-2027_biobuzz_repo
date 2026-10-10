package com.metalbenders.control.subsystems;

import android.annotation.SuppressLint;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.util.RotatingBuffer;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

public class DriveSubsystem extends SubsystemBase {

    private final Follower follower;
    private final RotatingBuffer<Pose> poseHistory = new RotatingBuffer<>(10);
    private final Telemetry telemetry;

    public DriveSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Pose initialPose) {
        this.telemetry = telemetry;
        this.follower = Constants.create(hardwareMap);
        this.follower.setPose(initialPose);
    }

    public void drive(double forward, double strafe, double heading, boolean fieldCentric) {
        if (fieldCentric) {
            ManualDrive.driveOrHold(follower,
                    ManualDrive.fieldCentric(forward, strafe, heading, follower.pose().heading()));
        } else {
            ManualDrive.driveOrHold(follower, forward, strafe, heading);
        }
    }

    public void followPath(Path path) {
        follower.follow(path);
    }

    public void resetHeading() {
        this.follower.setPose(new Pose(follower.pose().x(), follower.pose().y(), Math.toRadians(180)));
    }

    public Pose getCurrentPose() {
        return follower.pose();
    }

    public boolean isBusy() {
        return follower.isBusy();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void periodic() {
        poseHistory.put(follower.pose());
        follower.update();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Pose",
                String.format("x: %.2f, y: %.2f, heading: %.2f",
                        follower.pose().x(), follower.pose().y(), follower.pose().heading()));
        telemetry.addData("Driving", follower.isBusy() ? "Yes" : "No");
        telemetry.addLine();
    }
}
