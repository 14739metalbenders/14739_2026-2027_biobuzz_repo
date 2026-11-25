package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandBase;

public class AutoAbort {
    private boolean requested;

    public boolean isRequested() {
        return requested;
    }

    public Command requestCommand() {
        // Stay active until the enclosing race interrupts the strategy.
        return new CommandBase() {
            @Override
            public void initialize() {
                requested = true;
            }
        };
    }
}
