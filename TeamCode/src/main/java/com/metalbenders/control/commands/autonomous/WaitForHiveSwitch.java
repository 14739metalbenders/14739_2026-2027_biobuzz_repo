package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.subsystems.LimelightSubsystem;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;

public class WaitForHiveSwitch extends ParallelRaceGroup {

    public WaitForHiveSwitch(LimelightSubsystem limelightSubsystem, Supplier<FieldSide> expectedFieldSide) {
        super (
                new WaitCommand(MILLIS_TO_WAIT_FOR_HIVE_SWITCH),
                new WaitUntilCommand(() -> {
                    Constants.HiveState hiveState = limelightSubsystem.getHiveState();
                    return hiveState != null && hiveState.getFieldSide() == expectedFieldSide.get() && hiveState.isActive();
                }));
    }
}
