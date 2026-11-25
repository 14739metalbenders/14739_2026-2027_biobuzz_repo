package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RaceSafeSequentialCommandGroupTest {
    @Test
    public void sequenceFinishesFirstWithoutBeingInterruptedAgain() {
        TestCommand strategy = new TestCommand(true);
        TestCommand timer = new TestCommand(false);
        ParallelRaceGroup race = new ParallelRaceGroup(
                new RaceSafeSequentialCommandGroup(new SequentialCommandGroup(strategy)), timer);

        race.initialize();
        race.execute();
        assertTrue(race.isFinished());
        race.end(false);

        assertEquals(1, strategy.normalEnds);
        assertEquals(0, strategy.interruptedEnds);
        assertEquals(1, timer.interruptedEnds);
    }

    @Test
    public void timerInterruptsRunningSequenceAndSequenceCanBeReused() {
        TestCommand strategy = new TestCommand(false);
        TestCommand timer = new TestCommand(true);
        RaceSafeSequentialCommandGroup sequence = new RaceSafeSequentialCommandGroup(strategy);
        ParallelRaceGroup race = new ParallelRaceGroup(sequence, timer);

        race.initialize();
        race.execute();
        assertTrue(race.isFinished());
        race.end(false);
        assertEquals(1, strategy.interruptedEnds);
        assertEquals(1, timer.normalEnds);
        assertTrue(sequence.isFinished());

        timer.finished = false;
        race.initialize();
        assertFalse(sequence.isFinished());
        race.end(true);
        assertEquals(2, strategy.interruptedEnds);
    }

    @Test
    public void emptySequenceFinishesSafely() {
        TestCommand timer = new TestCommand(false);
        ParallelRaceGroup race = new ParallelRaceGroup(
                new RaceSafeSequentialCommandGroup(new SequentialCommandGroup()), timer);

        race.initialize();
        race.execute();
        assertTrue(race.isFinished());
        race.end(false);
        assertEquals(1, timer.interruptedEnds);
    }

    private static class TestCommand extends CommandBase {
        private boolean finished;
        private int normalEnds;
        private int interruptedEnds;

        TestCommand(boolean finished) {
            this.finished = finished;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void end(boolean interrupted) {
            if (interrupted) {
                interruptedEnds++;
            } else {
                normalEnds++;
            }
        }
    }
}
