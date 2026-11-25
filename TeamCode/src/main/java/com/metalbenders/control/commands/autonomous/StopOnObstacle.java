package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import java.util.function.BooleanSupplier;

public class StopOnObstacle extends SequentialCommandGroup {
    private final BooleanSupplier obstacleDetected;
    private final Runnable stopDrive;
    private boolean active;
    private boolean finished;

    public StopOnObstacle(Command driving, BooleanSupplier obstacleDetected, Runnable stopDrive) {
        super(driving);
        this.obstacleDetected = obstacleDetected;
        this.stopDrive = stopDrive;
    }

    @Override
    public void initialize() {
        finished = obstacleDetected.getAsBoolean();
        active = !finished;
        if (active) {
            super.initialize();
        } else {
            stopDrive.run();
        }
    }

    @Override
    public void execute() {
        if (active && obstacleDetected.getAsBoolean()) {
            super.end(true);
            active = false;
            finished = true;
            stopDrive.run();
        } else if (active) {
            super.execute();
        }
    }

    @Override
    public boolean isFinished() {
        return finished || (active && super.isFinished());
    }

    @Override
    public void end(boolean interrupted) {
        if (active) {
            super.end(interrupted);
            active = false;
        }
        finished = true;
    }
}
