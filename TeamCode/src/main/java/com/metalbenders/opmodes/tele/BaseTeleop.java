package com.metalbenders.opmodes.tele;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.control.commands.DriveCommand;
import com.metalbenders.control.commands.IntakeCommand;
import com.metalbenders.control.commands.ResetHeadingCommand;
import com.metalbenders.control.commands.ReverseIntakeCommand;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.util.AutonomousStorage;
import com.pedropathing.math.Pose;

public abstract class BaseTeleop extends BaseOpMode {

    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private GamepadEx driverGamepad;

    @Override
    public Pose getInitialPose() {
        return AutonomousStorage.endPose == null ? DEFAULT_POSE : AutonomousStorage.endPose;
    }

    @Override
    public void initialize() {
        driveSubsystem = new DriveSubsystem(getInitialPose());
        intakeSubsystem = new IntakeSubsystem(hardwareMap);

        driverGamepad = new GamepadEx(gamepad1);

        driveSubsystem.setDefaultCommand(new DriveCommand(
                driveSubsystem,
                () -> driverGamepad.getLeftY(),  // Positive value is forward
                () -> driverGamepad.getLeftX(),  // Positive value is strafe left
                () -> driverGamepad.getRightX()  // Positive value is turn counter-clockwise
        ));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new ResetHeadingCommand(driveSubsystem));

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileHeld(new ReverseIntakeCommand(intakeSubsystem));

        Trigger leftTrigger = new Trigger(() ->
                driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger.whileActiveContinuous(new IntakeCommand(intakeSubsystem));
    }
}
