package com.metalbenders.control.commands.shoot;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.Constants.IntakeState.SHOOT;
import static com.metalbenders.constants.Constants.LaunchGateState.OPEN;

public class StartShooting extends SequentialCommandGroup {

    public StartShooting(SubsystemManager subsystemManager) {
        addCommands(
                new SetLaunchGateState(subsystemManager, OPEN),
                new RunIntake(subsystemManager, SHOOT)
        );
        //Note: turretSubsystem is not used in this command, but it is added as a requirement so the turret doesn't
        // move while shooting
        addRequirements(
                subsystemManager.getIntakeSubsystem(),
                subsystemManager.getLaunchSubsystem(),
                subsystemManager.getTurretSubsystem());
    }
}
