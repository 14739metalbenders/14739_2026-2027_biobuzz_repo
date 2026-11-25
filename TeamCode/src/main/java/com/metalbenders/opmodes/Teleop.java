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
        subsystemManager.getDriveSubsystem().setDefaultCommand(
                new Drive(
                        subsystemManager,
                        () -> driverGamepad.getLeftX(),
                        () -> driverGamepad.getLeftY(),
                        () -> -driverGamepad.getRightX()
                ));

        //REVERSE INTAKE
        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whileActiveOnce(new RunIntake(subsystemManager, REVERSE))
                .whenInactive(new StopIntake(subsystemManager));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whileActiveOnce(new SetFlowerToolState(subsystemManager, DOWN))
                .whenInactive(new SetFlowerToolState(subsystemManager, UP));

        //INTAKE
        Trigger leftTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2);
        leftTrigger
                .whileActiveOnce(
                        new SequentialCommandGroup(
                                new SetLaunchGateState(subsystemManager, CLOSED),
                                new RunIntake(subsystemManager, INTAKE)))
                .whenInactive(new StopIntake(subsystemManager));

        //SHOOT
        Trigger rightTrigger = new Trigger(() -> driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2);
        rightTrigger
                .whileActiveOnce(new StartShooting(subsystemManager))
                .whenInactive(new StopShooting(subsystemManager));
    }
}
