package com.metalbenders.opmodes;

import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.IntakeState.REVERSE_INTAKE;
import static com.metalbenders.constants.Constants.IntakeState.SHOOT;
import static com.metalbenders.constants.Constants.IntakeState.STOP;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;
import static com.metalbenders.constants.Constants.LaunchGateState.KICK;
import static com.metalbenders.constants.Constants.LaunchGateState.OPEN;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.control.commands.*;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Teleop", group = "Teleop")
public class Teleop extends Base {

    @Override
    public void initialize() {
        super.initialize();

        //DRIVE
        driveSubsystem.setDefaultCommand(
                new Drive(
                        driveSubsystem,
                        () -> driverGamepad.getLeftX(),
                        () -> driverGamepad.getLeftY(),
                        () -> -driverGamepad.getRightX()
        ));

        //REVERSE INTAKE
        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileActiveOnce(new SetIntakeState(intakeSubsystem, REVERSE_INTAKE));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whileActiveOnce(new SetFlowerToolState(flowerToolSubsystem, DOWN))
                .whenInactive(new SetFlowerToolState(flowerToolSubsystem, UP));


        //INTAKE
        Trigger leftTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger
                .whileActiveContinuous(
                        new SequentialCommandGroup(
                            new SetLaunchGateState(launchSubsystem, CLOSED),
                            new SetIntakeState(intakeSubsystem, INTAKE)));

        //SHOOT
        Trigger rightTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger
                .whileActiveOnce(new SequentialCommandGroup(
                    new SetLaunchGateState(launchSubsystem, OPEN),
                    new SetIntakeState(intakeSubsystem, SHOOT)))
                .whenInactive(new SequentialCommandGroup(
                    new SetIntakeState(intakeSubsystem, STOP),
                    new SetLaunchGateState(launchSubsystem, KICK),
                    new SetLaunchGateState(launchSubsystem, CLOSED)));
    }
}
