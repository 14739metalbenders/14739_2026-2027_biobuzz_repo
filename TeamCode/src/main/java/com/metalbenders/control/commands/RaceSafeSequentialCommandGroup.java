package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

/**
 * Keeps a finished sequence finished after end(), as required by FTCLib's race group.
 */
public class RaceSafeSequentialCommandGroup extends SequentialCommandGroup {
    private boolean ended;

    public RaceSafeSequentialCommandGroup(Command... commands) {
        super(commands);
    }

    @Override
    public void initialize() {
        ended = false;
        super.initialize();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        ended = true;
    }

    @Override
    public boolean isFinished() {
        return ended || super.isFinished();
    }
}
