package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import java.util.function.BooleanSupplier;

public final class NoCrossShooting {
    private NoCrossShooting() {
    }

    public static Command create(BooleanSupplier inLaunchArea, Command waitForHive,
                                 Command shoot, AutoAbort abort) {
        return new ConditionalCommand(
                new SequentialCommandGroup(waitForHive, shoot),
                abort.requestCommand(),
                inLaunchArea);
    }
}
