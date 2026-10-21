package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.Subsystem;
import com.metalbenders.constants.Constants;

public abstract class CloseableSubsystem implements Subsystem {

    public abstract void close(Constants.OpModeType opModeType);
}
