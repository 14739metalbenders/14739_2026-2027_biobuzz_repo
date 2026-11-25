package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoCrossShootingTest {
    @Test
    public void lidarStopAbortsOutsideLaunchAreaButWaitsInside() {
        for (boolean inArea : new boolean[]{false, true}) {
            List<String> events = new ArrayList<>();
            AtomicBoolean obstacle = new AtomicBoolean();
            AutoAbort abort = new AutoAbort();
            Command drive = new CommandBase() {
                @Override
                public void end(boolean interrupted) {
                    if (interrupted) {
                        events.add("drive interrupted");
                    }
                }
            };
            Command strategy = new SequentialCommandGroup(
                    new StopOnObstacle(drive, obstacle::get, () -> events.add("drive stopped")),
                    NoCrossShooting.create(() -> inArea,
                            new WaitForVisibleHive(() -> false, () -> {},
                                    () -> events.add("stop targeting"), 500),
                            new InstantCommand(() -> events.add("shoot")), abort));
            Command sequence = AutonomousSequence.withoutPark(strategy, abort,
                    new InstantCommand(() -> events.add("cleanup")));
            sequence.initialize();
            obstacle.set(true);
            for (int i = 0; i < 10 && !sequence.isFinished(); i++) {
                sequence.execute();
            }
            assertEquals(!inArea, abort.isRequested());
            assertEquals(!inArea, sequence.isFinished());
            sequence.end(inArea);
            assertEquals(inArea
                            ? Arrays.asList("drive interrupted", "drive stopped", "stop targeting")
                            : Arrays.asList("drive interrupted", "drive stopped", "cleanup"),
                    events);
        }
    }

    @Test
    public void noParkWaitsUntilStoppedWhenHiveNeverAppears() {
        List<String> events = new ArrayList<>();
        AutoAbort abort = new AutoAbort();
        Command strategy = NoCrossShooting.create(() -> true,
                new WaitForVisibleHive(() -> false, () -> {},
                        () -> events.add("stop targeting"), 500),
                new InstantCommand(() -> events.add("shoot")), abort);
        Command sequence = AutonomousSequence.withoutPark(strategy, abort,
                new InstantCommand(() -> events.add("cleanup")));
        sequence.initialize();
        for (int i = 0; i < 10; i++) {
            sequence.execute();
            assertFalse(sequence.isFinished());
        }
        sequence.end(true);
        assertFalse(abort.isRequested());
        assertEquals(Arrays.asList("stop targeting"), events);
    }

    @Test
    public void outsideLaunchAreaAbortsWithoutWaitingOrShooting() {
        AutoAbort abort = new AutoAbort();
        List<String> events = new ArrayList<>();
        Command sequence = AutonomousSequence.withoutPark(
                NoCrossShooting.create(() -> false,
                        new InstantCommand(() -> events.add("wait")),
                        new InstantCommand(() -> events.add("shoot")), abort),
                abort, new InstantCommand(() -> events.add("cleanup")));
        run(sequence, 5);
        assertTrue(abort.isRequested());
        assertEquals(Arrays.asList("cleanup"), events);
    }

    @Test
    public void waitingIsInterruptedByParkOverrideWithoutShooting() {
        List<String> events = new ArrayList<>();
        AutoAbort abort = new AutoAbort();
        AtomicBoolean timerExpired = new AtomicBoolean();
        Command wait = new WaitForVisibleHive(() -> false, () -> {},
                () -> events.add("stop targeting"), 500);
        Command strategy = NoCrossShooting.create(() -> true, wait,
                new InstantCommand(() -> events.add("shoot")), abort);
        Command timeout = new CommandBase() {
            @Override
            public boolean isFinished() {
                return timerExpired.get();
            }
        };
        Command sequence = AutonomousSequence.withPark(strategy, timeout, abort,
                new InstantCommand(() -> events.add("cleanup")),
                new InstantCommand(() -> events.add("park")),
                new InstantCommand(() -> events.add("park shot")), () -> false);
        sequence.initialize();
        sequence.execute();
        assertFalse(sequence.isFinished());
        timerExpired.set(true);
        for (int i = 0; i < 10 && !sequence.isFinished(); i++) {
            sequence.execute();
        }
        assertTrue(sequence.isFinished());
        sequence.end(false);
        assertEquals(Arrays.asList("stop targeting", "cleanup", "park"), events);
    }

    @Test
    public void visibilityLostDuringDelayWaitsUntilStableBeforeNextChallenge() {
        AtomicBoolean visible = new AtomicBoolean();
        AtomicLong clock = new AtomicLong();
        List<String> events = new ArrayList<>();
        AutoAbort abort = new AutoAbort();
        Command wait = new WaitForVisibleHive(visible::get, () -> {},
                () -> events.add("stop targeting"), 500, clock::get);
        Command strategy = new SequentialCommandGroup(
                NoCrossShooting.create(() -> true, wait,
                        new InstantCommand(() -> events.add("shoot")), abort),
                new InstantCommand(() -> events.add("next")));
        Command sequence = AutonomousSequence.withoutPark(strategy, abort,
                new InstantCommand(() -> events.add("cleanup")));
        sequence.initialize();
        sequence.execute();
        visible.set(true);
        sequence.execute();
        clock.set(400_000_000L);
        visible.set(false);
        sequence.execute();
        clock.set(500_000_000L);
        visible.set(true);
        sequence.execute();
        clock.set(999_000_000L);
        sequence.execute();
        assertTrue(events.isEmpty());
        clock.set(1_000_000_000L);
        for (int i = 0; i < 10 && !sequence.isFinished(); i++) {
            sequence.execute();
        }
        assertTrue(sequence.isFinished());
        sequence.end(false);
        assertEquals(Arrays.asList("stop targeting", "shoot", "next"), events);
    }

    private void run(Command sequence, int cycles) {
        sequence.initialize();
        for (int i = 0; i < cycles && !sequence.isFinished(); i++) {
            sequence.execute();
        }
        assertTrue(sequence.isFinished());
        sequence.end(false);
    }
}
