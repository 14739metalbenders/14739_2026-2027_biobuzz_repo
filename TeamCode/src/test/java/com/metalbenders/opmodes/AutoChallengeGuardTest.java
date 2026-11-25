package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.InstantCommand;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AutoChallengeGuardTest {
    @Test
    public void currentSideMismatchSkipsCollectionWithoutInitializingIt() {
        AtomicBoolean started = new AtomicBoolean();
        Command guarded = Auto.guardCollection(
                new InstantCommand(() -> started.set(true)), () -> false);
        guarded.initialize();
        assertTrue(guarded.isFinished());
        guarded.end(false);
        assertFalse(started.get());
    }

    @Test
    public void currentSideMatchRunsCollection() {
        AtomicBoolean started = new AtomicBoolean();
        Command guarded = Auto.guardCollection(
                new InstantCommand(() -> started.set(true)), () -> true);
        guarded.initialize();
        assertTrue(started.get());
        guarded.end(false);
    }
}
