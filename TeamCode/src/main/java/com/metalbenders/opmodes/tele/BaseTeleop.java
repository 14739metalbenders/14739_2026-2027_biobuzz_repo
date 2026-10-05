package com.metalbenders.opmodes.tele;

import static com.metalbenders.constants.Constants.DEFAULT_POSE;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.control.commands.DefaultDriveCommand;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.util.AutonomousStorage;
import com.pedropathing.math.Pose;

public abstract class BaseTeleop extends BaseOpMode {

    private DriveSubsystem driveSubsystem;
    private GamepadEx driverGamepad;

    @Override
    public Pose getInitialPose() {
        return AutonomousStorage.endPose == null ? DEFAULT_POSE : AutonomousStorage.endPose;
    }

    @Override
    public void initialize() {
        // Initialize subsystems and gamepads
        driveSubsystem = new DriveSubsystem(getInitialPose());
        driverGamepad = new GamepadEx(gamepad1);

        // Bind joystick inputs directly into the background command
        driveSubsystem.setDefaultCommand(new DefaultDriveCommand(
                driveSubsystem,
                () -> driverGamepad.getLeftY(),  // Positive value is forward
                () -> driverGamepad.getLeftX(),  // Positive value is strafe left
                () -> driverGamepad.getRightX()  // Positive value is turn counter-clockwise
        ));

        // Optional: Reset heading anytime when pressing the "Y" button (Field-Centric drift fix)
        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(driveSubsystem::resetHeading);
    }
}
