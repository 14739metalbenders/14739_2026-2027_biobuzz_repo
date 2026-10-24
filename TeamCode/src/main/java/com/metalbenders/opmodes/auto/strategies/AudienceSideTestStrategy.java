package com.metalbenders.opmodes.auto.strategies;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.DriveToPositionCommand;
import com.metalbenders.control.subsystems.*;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.FieldLocation.AUDIENCE_SIDE_LAUNCH;

public class AudienceSideTestStrategy extends AutoStrategy {

    @Override
    public void initialize(
            Constants.AllianceColor allianceColor,
            Pose parkPose,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem) {

        // Initialize any necessary components or variables for the Audience Side Test Strategy
        CommandScheduler.getInstance().schedule(
                new SequentialCommandGroup(
                    new DriveToPositionCommand(driveSubsystem, AUDIENCE_SIDE_LAUNCH.getPose(allianceColor)))
        );
    }

    @Override
    public Constants.FieldSide getStartingFieldSide() {
        return Constants.FieldSide.AUDIENCE_SIDE;
    }
}
