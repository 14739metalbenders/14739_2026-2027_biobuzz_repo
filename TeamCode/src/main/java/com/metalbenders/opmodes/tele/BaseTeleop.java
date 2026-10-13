package com.metalbenders.opmodes.tele;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.DriveCommand;
import com.metalbenders.control.commands.IntakeCommand;
import com.metalbenders.control.commands.ResetHeadingCommand;
import com.metalbenders.control.commands.ReverseIntakeCommand;
import com.metalbenders.control.commands.ShootCommand;
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

        driveSubsystem.setDefaultCommand(new DriveCommand(
                driveSubsystem,
                () -> driverGamepad.getLeftX(),
                () -> driverGamepad.getLeftY(),
                () -> -driverGamepad.getRightX()
        ));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new ResetHeadingCommand(driveSubsystem));

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileHeld(new ReverseIntakeCommand(intakeSubsystem));

        Trigger leftTrigger = new Trigger(() ->
                driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger.whileActiveContinuous(new IntakeCommand(intakeSubsystem));

        Trigger rightTrigger = new Trigger(() ->
                driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger.whileActiveContinuous(new ShootCommand(turretSubsystem));
    }
}
