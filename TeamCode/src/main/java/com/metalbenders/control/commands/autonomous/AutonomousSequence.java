package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.metalbenders.control.commands.RaceSafeSequentialCommandGroup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public final class AutonomousSequence {
    private AutonomousSequence() {
    }

    public static SequentialCommandGroup withPark(
            Command strategy, Command timeout, AutoAbort abort, Command cleanup, Command park,
            Command parkShot, BooleanSupplier hiveVisible) {
        AtomicBoolean timerExpired = new AtomicBoolean();
        Command trackedTimeout = new RaceSafeSequentialCommandGroup(timeout) {
            @Override
            public void initialize() {
                timerExpired.set(false);
                super.initialize();
            }

            @Override
            public void execute() {
                super.execute();
                if (super.isFinished()) {
                    timerExpired.set(true);
                }
            }
        };
        return new SequentialCommandGroup(
                new ParallelRaceGroup(
                        new RaceSafeSequentialCommandGroup(strategy),
                        trackedTimeout,
                        new WaitUntilCommand(abort::isRequested)),
                cleanup,
                park,
                new ConditionalCommand(parkShot, new InstantCommand(),
                        () -> timerExpired.get() && !abort.isRequested() && hiveVisible.getAsBoolean()));
    }

    public static SequentialCommandGroup withoutPark(
            Command strategy, AutoAbort abort, Command cleanup) {
        return new SequentialCommandGroup(
                new ParallelRaceGroup(
                        new RaceSafeSequentialCommandGroup(strategy),
                        new WaitUntilCommand(abort::isRequested)),
                new ConditionalCommand(cleanup, new InstantCommand(), abort::isRequested));
    }
}
