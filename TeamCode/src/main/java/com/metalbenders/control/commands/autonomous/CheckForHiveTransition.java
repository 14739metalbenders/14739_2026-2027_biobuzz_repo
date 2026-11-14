package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.subsystems.LimelightSubsystem;

public class CheckForHiveTransition extends ParallelRaceGroup {

    public CheckForHiveTransition(LimelightSubsystem limelightSubsystem, long timeToWaitForTransitionStateMillis) {
        super(
                new WaitCommand(timeToWaitForTransitionStateMillis),
                new WaitUntilCommand(() -> {
                    Constants.HiveState hiveState = limelightSubsystem.getHiveState();
                    return limelightSubsystem.isHiveDetected() && hiveState != null && hiveState.isTransitioning();
                })
        );
    }
}
