package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.metalbenders.control.commands.drive.Drive;
import com.metalbenders.control.commands.flowertool.SetFlowerToolState;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.shoot.StartShooting;
import com.metalbenders.control.commands.shoot.StopShooting;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static com.metalbenders.constants.Constants.FlowerToolState.DOWN;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.IntakeState.REVERSE;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;

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
                .whileActiveOnce(new RunIntake(intakeSubsystem, REVERSE))
                .whenInactive(new StopIntake(intakeSubsystem));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whileActiveOnce(new SetFlowerToolState(flowerToolSubsystem, DOWN))
                .whenInactive(new SetFlowerToolState(flowerToolSubsystem, UP));

        //INTAKE
        Trigger leftTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger
                .whileActiveOnce(
                        new SequentialCommandGroup(
                                new SetLaunchGateState(launchSubsystem, CLOSED),
                                new RunIntake(intakeSubsystem, INTAKE)))
                .whenInactive(new StopIntake(intakeSubsystem));

        //SHOOT
        Trigger rightTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger
                .whileActiveOnce(new StartShooting(intakeSubsystem, launchSubsystem, turretSubsystem))
                .whenInactive(new StopShooting(intakeSubsystem, launchSubsystem));
    }
}
