package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.CommandBase;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

public class WaitForVisibleHive extends CommandBase {
    private final BooleanSupplier hiveVisible;
    private final Runnable updateTargeting;
    private final Runnable stopTargeting;
    private final LongSupplier clock;
    private final long stableNanos;
    private long visibleSince;
    private boolean tracking;
    private boolean finished;

    public WaitForVisibleHive(BooleanSupplier hiveVisible, Runnable updateTargeting,
                              Runnable stopTargeting, long stableMillis) {
        this(hiveVisible, updateTargeting, stopTargeting, stableMillis, System::nanoTime);
    }

    WaitForVisibleHive(BooleanSupplier hiveVisible, Runnable updateTargeting,
                       Runnable stopTargeting, long stableMillis, LongSupplier clock) {
        this.hiveVisible = hiveVisible;
        this.updateTargeting = updateTargeting;
        this.stopTargeting = stopTargeting;
        this.stableNanos = stableMillis * 1_000_000L;
        this.clock = clock;
    }

    @Override
    public void initialize() {
        tracking = false;
        finished = false;
    }

    @Override
    public void execute() {
        updateTargeting.run();
        if (!hiveVisible.getAsBoolean()) {
            tracking = false;
            return;
        }
        long now = clock.getAsLong();
        if (!tracking) {
            tracking = true;
            visibleSince = now;
        }
        finished = now - visibleSince >= stableNanos;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void end(boolean interrupted) {
        stopTargeting.run();
    }
}
