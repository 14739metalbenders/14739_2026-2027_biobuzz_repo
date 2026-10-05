package com.metalbenders.control.subsystems;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.util.TimedEntity;
import com.metalbenders.util.RotatingBuffer;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

public class DriveSubsystem extends SubsystemBase {

    private final Follower follower;
    private final RotatingBuffer<TimedEntity<Pose>> poseHistory = new RotatingBuffer<>(10);

    public DriveSubsystem(Pose initialPose) {
        follower = Constants.create(hardwareMap);
        follower.setPose(initialPose);
    }

    public void drive(double forward, double strafe, double heading, boolean fieldCentric) {
        if (fieldCentric) {
            ManualDrive.driveOrHold(follower,
                    ManualDrive.fieldCentric(
                        forward,
                        strafe,
                        heading,
                        follower.pose().heading()));
        } else {
            ManualDrive.driveOrHold(follower, forward, strafe, heading);
        }
    }

    public void resetHeading() {
        follower.setPose(new Pose(
                follower.pose().x(),
                follower.pose().y(),
                0.0 // Resets angle to zero
        ));
    }

    @Override
    public void periodic() {
        poseHistory.put(TimedEntity.of(follower.pose()));
        follower.update();
    }

    public List<TimedEntity<Pose>> getPoseHistory() {
        return poseHistory.toList();
    }

    public Follower getFollower() {
        return follower;
    }
}
