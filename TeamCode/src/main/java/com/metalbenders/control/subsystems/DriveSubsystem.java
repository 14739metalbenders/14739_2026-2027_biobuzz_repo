package com.metalbenders.control.subsystems;

import static com.metalbenders.constants.Constants.OpModeType.AUTO;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.util.AutonomousStorage;
import com.metalbenders.util.RotatingBuffer;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.hardware.HardwareMap;
import static com.pedropathing.api.Paths.*;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Locale;

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

    public void driveToPosition(Pose pose) {
        follower.follow(line(follower.pose(), pose).constant(pose.heading()));
    }

    public void resetHeading() {
        this.follower.setHeading(Math.toRadians(180));
    }

    public Pose getCurrentPose() {
        return follower.pose();
    }

    public Twist getCurrentTwist() { return follower.twist(); }

    public boolean isBusy() {
        return follower.isBusy();
    }

    @Override
    public void periodic() {
        poseHistory.put(follower.pose());
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

    public void teardown(com.metalbenders.constants.Constants.OpModeType opModeType) {
        follower.stop();
        if (opModeType == AUTO) {
            AutonomousStorage.endPose = getCurrentPose();
        }
    }
}
