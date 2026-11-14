package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.LimelightSubsystem;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.MAX_TIME_TO_WAIT_FOR_TRANSITION_WHEN_STARTING_INACTIVE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;

public class WaitForHiveSwitch extends SequentialCommandGroup {

    public WaitForHiveSwitch(LimelightSubsystem limelightSubsystem, Supplier<FieldSide> expectedFieldSide) {
        super (
                new CheckForHiveTransition(limelightSubsystem, MAX_TIME_TO_WAIT_FOR_TRANSITION_WHEN_STARTING_INACTIVE),
                new ConditionalCommand(
                        new ParallelRaceGroup(
                                new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH),
                                new WaitUntilCommand(() -> {
                                    Constants.HiveState hiveState = limelightSubsystem.getHiveState();
                                    return hiveState != null && hiveState.getFieldSide() == expectedFieldSide.get() && hiveState.isActive();
                                })),
                        new InstantCommand(),
                        () -> {
                            Constants.HiveState hiveState = limelightSubsystem.getHiveState();
                            return hiveState != null && hiveState.getFieldSide() == expectedFieldSide.get() && hiveState.isTransitioning();
                        })
                );
    }
}
