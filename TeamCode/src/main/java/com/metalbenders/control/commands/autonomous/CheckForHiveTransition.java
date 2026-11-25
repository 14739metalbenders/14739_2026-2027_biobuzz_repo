package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.SubsystemManager;

public class CheckForHiveTransition extends ParallelRaceGroup {

    public CheckForHiveTransition(SubsystemManager subsystemManager, long timeToWaitForTransitionStateMillis) {
        super(
                new WaitCommand(timeToWaitForTransitionStateMillis),
                new WaitUntilCommand(() -> {
                    Constants.HiveState hiveState = subsystemManager.getLimelightSubsystem().getHiveState();
                    return subsystemManager.getLimelightSubsystem().isHiveDetected() && hiveState != null && hiveState.isTransitioning();
                })
        );
    }
}
