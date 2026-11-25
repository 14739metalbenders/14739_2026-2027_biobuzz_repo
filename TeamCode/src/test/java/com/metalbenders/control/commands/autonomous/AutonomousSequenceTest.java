package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.ConditionalCommand;
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

public class AutonomousSequenceTest {
    @Test
    public void failedShotWithParkSkipsFollowingActionsAndParks() {
        assertSequence(true, false, "arrive", "cleanup", "park");
    }

    @Test
    public void failedShotWithoutParkSkipsFollowingActionsAndStops() {
        assertSequence(false, false, "arrive", "cleanup");
    }

    @Test
    public void successfulShotContinuesAndParksWhenConfigured() {
        assertSequence(true, true, "arrive", "shoot", "next", "cleanup", "park");
    }

    @Test
    public void successfulShotWithoutParkContinuesWithoutCleanup() {
        assertSequence(false, true, "arrive", "shoot", "next");
    }

    @Test
    public void timerInterruptsStrategyThenShootsWhenHiveVisible() {
        assertTimerParkShot(true, "interrupted", "cleanup", "park", "park shot");
    }

    @Test
    public void timerDoesNotShootWhenHiveNotVisible() {
        assertTimerParkShot(false, "interrupted", "cleanup", "park");
    }

    private void assertTimerParkShot(boolean hiveVisible, String... expected) {
        List<String> events = new ArrayList<>();
        AutoAbort abort = new AutoAbort();
        Command running = new CommandBase() {
            @Override
            public void end(boolean interrupted) {
                if (interrupted) {
                    events.add("interrupted");
                }
            }
        };
        Command sequence = AutonomousSequence.withPark(
                running, new TestTimeout(true), abort,
                new InstantCommand(() -> events.add("cleanup")),
                new InstantCommand(() -> events.add("park")),
                new InstantCommand(() -> events.add("park shot")),
                () -> hiveVisible);
        runToCompletion(sequence);
        assertFalse(abort.isRequested());
        assertEquals(Arrays.asList(expected), events);
    }

    @Test
    public void hiveVisibilityIsCheckedAfterPark() {
        List<String> events = new ArrayList<>();
        AtomicBoolean visible = new AtomicBoolean(false);
        Command sequence = AutonomousSequence.withPark(
                new TestTimeout(false), new TestTimeout(true), new AutoAbort(),
                new InstantCommand(),
                new InstantCommand(() -> visible.set(true)),
                new InstantCommand(() -> events.add("park shot")),
                visible::get);
        runToCompletion(sequence);
        assertEquals(Arrays.asList("park shot"), events);
    }

    private void assertSequence(boolean parkEnabled, boolean canShoot, String... expected) {
        List<String> events = new ArrayList<>();
        AutoAbort abort = new AutoAbort();
        Command strategy = new SequentialCommandGroup(
                new InstantCommand(() -> events.add("arrive")),
                new ConditionalCommand(
                        new InstantCommand(() -> events.add("shoot")),
                        abort.requestCommand(),
                        () -> canShoot),
                new InstantCommand(() -> events.add("next")));
        Command cleanup = new InstantCommand(() -> events.add("cleanup"));
        Command sequence = parkEnabled
                ? AutonomousSequence.withPark(strategy, new TestTimeout(false), abort,
                        cleanup, new InstantCommand(() -> events.add("park")),
                        new InstantCommand(() -> events.add("park shot")), () -> true)
                : AutonomousSequence.withoutPark(strategy, abort, cleanup);

        runToCompletion(sequence);
        assertEquals(Arrays.asList(expected), events);
    }

    private void runToCompletion(Command sequence) {
        sequence.initialize();
        for (int i = 0; i < 10 && !sequence.isFinished(); i++) {
            sequence.execute();
        }
        assertTrue("sequence should finish", sequence.isFinished());
        sequence.end(false);
    }

    private static class TestTimeout extends CommandBase {
        private final boolean finished;

        TestTimeout(boolean finished) {
            this.finished = finished;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }
    }
}
