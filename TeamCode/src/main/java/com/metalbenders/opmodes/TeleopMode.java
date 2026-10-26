package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.*;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Teleop", group = "Teleop")
public abstract class TeleopMode extends BaseOpMode {

    @Override
    public void initialize() {
        super.initialize();

        //DRIVE
        driveSubsystem.setDefaultCommand(
                new DriveCommand(
                        driveSubsystem,
                        () -> driverGamepad.getLeftX(),
                        () -> driverGamepad.getLeftY(),
                        () -> -driverGamepad.getRightX()
        ));

        //SET ACTIVE FIELD SIDE TO SCORE SIDE
        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(new SetFieldSideOverrideCommand(limelightSubsystem, Constants.FieldSide.SCORE_SIDE));

        //SET ACTIVE FIELD SIDE TO AUDIENCE SIDE
        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new SetFieldSideOverrideCommand(limelightSubsystem, Constants.FieldSide.AUDIENCE_SIDE));

        //REVERSE INTAKE
        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileActiveOnce(new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.REVERSE_INTAKE));

        //INTAKE
        Trigger leftTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger.whileActiveOnce(new SequentialCommandGroup(
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.CLOSED),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.INTAKE)));

        //SHOOT
        Trigger rightTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger.whileActiveOnce(new SequentialCommandGroup(
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.OPEN),
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.INTAKE)));
        rightTrigger.whenInactive(new SequentialCommandGroup(
                new SetIntakeStateCommand(intakeSubsystem, Constants.IntakeState.STOP),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.KICK),
                new SetLaunchGateStateCommand(launchSubsystem, Constants.LaunchGateState.CLOSED)));
    }
}
