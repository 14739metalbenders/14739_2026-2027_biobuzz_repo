package com.metalbenders.control.subsystems;


import com.arcrobotics.ftclib.command.SubsystemBase;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.hardware.HardwareMap;

import static com.metalbenders.constants.Constants.AUTO_SPEED_MULTIPLIER;
import static com.pedropathing.api.Paths.*;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Locale;

public class DriveSubsystem extends SubsystemBase {

    private final Follower follower;
    private final Telemetry telemetry;

    public DriveSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Pose initialPose) {
        this.telemetry = telemetry;
        this.follower = Constants.create(hardwareMap);
        this.follower.setPose(initialPose);
    }

    public void drive(double forward, double strafe, double heading) {
        ManualDrive.driveOrHold(follower,
                ManualDrive.fieldCentric(forward, strafe, heading, follower.pose().heading()));
    }

    public void driveToPosition(Pose pose, Double maxSpeed) {
        if (maxSpeed == null) {
            maxSpeed = 1.0;
        }
        follower.follow(
                line(follower.pose(), pose).constant(pose.heading())
                        .with(Constants.foresightConfig.maxPathSpeed.at(maxSpeed * AUTO_SPEED_MULTIPLIER)));
    }

    public Pose getCurrentPose() {
        return follower.pose();
    }

    public Twist getCurrentTwist() { return follower.twist(); }

    public boolean isBusy() {
        return follower.isBusy();
    }

    public void stop() {
        follower.stop();
    }

    @Override
    public void periodic() {
        follower.update();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Pose",
                String.format(Locale.ENGLISH, "x: %.2f, y: %.2f, heading: %.2f",
                        follower.pose().x(), follower.pose().y(), Math.toDegrees(follower.pose().heading())));
        telemetry.addData("Driving", follower.isBusy() ? "Yes" : "No");
        //telemetry.addData("Xvector", follower.twist().vx);
        //telemetry.addData("Yvector", follower.twist().vy);
        //telemetry.addData("Omega", follower.twist().omega);
        telemetry.addLine();
    }

    public void teardown() {
        stop();
    }
}
