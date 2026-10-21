package com.metalbenders.opmodes.tele;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.*;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.util.AutonomousStorage;
import com.pedropathing.math.Pose;

public abstract class BaseTeleop extends BaseOpMode {

    private GamepadEx driverGamepad;

    @Override
    public Constants.OpModeType getOpModeType() {
        return Constants.OpModeType.TELEOP;
    }

    @Override
    public Pose getInitialPose() {
        return AutonomousStorage.endPose == null ? DEFAULT_POSE : AutonomousStorage.endPose;
    }

    @Override
    public void initialize() {
        super.initialize();

        driverGamepad = new GamepadEx(gamepad1);

        getSubsystemMap().get(DriveSubsystem.class).setDefaultCommand(new DriveCommand(
                getSubsystemMap().get(DriveSubsystem.class),
                () -> driverGamepad.getLeftX(),
                () -> driverGamepad.getLeftY(),
                () -> -driverGamepad.getRightX()
        ));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(
                        new ResetHeadingCommand(
                                getSubsystemMap().get(DriveSubsystem.class)));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(
                        new SetFieldSideOverrideCommand(
                                getSubsystemMap().get(LimelightSubsystem.class),
                                Constants.FieldSide.SCORE_SIDE));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(
                        new SetFieldSideOverrideCommand(
                                getSubsystemMap().get(LimelightSubsystem.class),
                                Constants.FieldSide.AUDIENCE_SIDE));

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileHeld(
                        new ReverseIntakeCommand(
                                getSubsystemMap().get(IntakeSubsystem.class)));

        Trigger leftTrigger = new Trigger(() ->
                driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger.whileActiveContinuous(
                new IntakeCommand(
                        getSubsystemMap().get(IntakeSubsystem.class),
                        getSubsystemMap().get(TurretSubsystem.class)));

        Trigger rightTrigger = new Trigger(() ->
                driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger.whileActiveContinuous(
                new ShootCommand(
                        getSubsystemMap().get(TurretSubsystem.class),
                        getSubsystemMap().get(IntakeSubsystem.class)));
    }
}
