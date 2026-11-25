package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopOnObstacleTest {
    @Test
    public void obstacleBeforeStartDoesNotInitializeDriveAndAttemptsShot() {
        List<String> events = new ArrayList<>();
        AtomicBoolean obstacle = new AtomicBoolean(true);
        SequentialCommandGroup sequence = new SequentialCommandGroup(
                new StopOnObstacle(new DrivePhase("drive", events), obstacle::get,
                        () -> events.add("stop drivetrain")),
                new InstantCommand(() -> events.add("shoot")));

        sequence.initialize();
        sequence.execute();

        assertEquals(Arrays.asList("stop drivetrain", "shoot"), events);
        sequence.end(true);
    }

    @Test
    public void obstacleStopsEveryDrivingPhaseBeforeAttemptingShot() {
        for (int blockedPhase = 0; blockedPhase < 4; blockedPhase++) {
            List<String> events = new ArrayList<>();
            AtomicBoolean obstacle = new AtomicBoolean();
            DrivePhase[] phases = new DrivePhase[4];
            for (int i = 0; i < phases.length; i++) {
                phases[i] = new DrivePhase("drive" + i, events);
            }
            SequentialCommandGroup driving = new SequentialCommandGroup(
                    phases[0], new SequentialCommandGroup(phases[1], phases[2]), phases[3]);
            SequentialCommandGroup sequence = new SequentialCommandGroup(
                    new StopOnObstacle(driving, obstacle::get, () -> events.add("stop drivetrain")),
                    new InstantCommand(() -> events.add("shoot")));

            sequence.initialize();
            for (int i = 0; i < blockedPhase; i++) {
                phases[i].finished = true;
                sequence.execute();
            }
            obstacle.set(true);
            sequence.execute();

            assertEquals(1, phases[blockedPhase].interruptions);
            for (int i = blockedPhase + 1; i < phases.length; i++) {
                assertFalse(events.contains("start drive" + i));
            }
            assertEquals("stop drive" + blockedPhase, events.get(events.size() - 3));
            assertEquals("stop drivetrain", events.get(events.size() - 2));
            assertEquals("shoot", events.get(events.size() - 1));
            sequence.end(true);
        }
    }

    @Test
    public void clearPathCompletesNormallyAndCanBeReused() {
        List<String> events = new ArrayList<>();
        AtomicBoolean obstacle = new AtomicBoolean();
        DrivePhase drive = new DrivePhase("drive", events);
        StopOnObstacle guarded = new StopOnObstacle(drive, obstacle::get,
                () -> events.add("stop drivetrain"));

        guarded.initialize();
        assertFalse(guarded.isFinished());
        drive.finished = true;
        guarded.execute();
        assertTrue(guarded.isFinished());
        guarded.end(false);
        assertTrue(guarded.isFinished());
        assertEquals(0, drive.interruptions);

        drive.finished = false;
        guarded.initialize();
        assertFalse(guarded.isFinished());
        guarded.end(true);
        assertEquals(1, drive.interruptions);
    }

    private static class DrivePhase extends CommandBase {
        private final String name;
        private final List<String> events;
        private boolean finished;
        private int interruptions;

        DrivePhase(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public void initialize() {
            events.add("start " + name);
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void end(boolean interrupted) {
            if (interrupted) {
                interruptions++;
                events.add("stop " + name);
            }
        }
    }
}
