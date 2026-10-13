package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.control.commands.UpdateTurretPositionCommand;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;
import com.pedropathing.math.Pose;

public abstract class BaseOpMode extends CommandOpMode {

    public abstract AllianceColor getAllianceColor();
    public abstract Pose getInitialPose();
    public abstract Constants.OpModeType getOpModeType();

    protected DriveSubsystem driveSubsystem;
    protected IntakeSubsystem intakeSubsystem;
    protected LimelightSubsystem limelightSubsystem;
    protected TurretSubsystem turretSubsystem;

    @Override
    public void initialize() {
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, getInitialPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
//        limelightSubsystem = new LimelightSubsystem(hardwareMap, telemetry, getAllianceColor());
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry);

//        CommandScheduler.getInstance().setDefaultCommand(turretSubsystem,
//                new UpdateTurretPositionCommand(turretSubsystem, limelightSubsystem, driveSubsystem));
    }

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
