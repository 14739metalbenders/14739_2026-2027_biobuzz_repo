package com.metalbenders.control.commands.autonomous;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WaitForVisibleHiveTest {
    @Test
    public void waitsForContinuousVisibilityAndKeepsTargetingActive() {
        AtomicBoolean visible = new AtomicBoolean();
        AtomicLong clock = new AtomicLong();
        AtomicInteger updates = new AtomicInteger();
        AtomicInteger stops = new AtomicInteger();
        WaitForVisibleHive wait = new WaitForVisibleHive(visible::get, updates::incrementAndGet,
                stops::incrementAndGet, 500, clock::get);
        wait.initialize();
        wait.execute();
        assertFalse(wait.isFinished());
        visible.set(true);
        wait.execute();
        clock.set(499_000_000L);
        wait.execute();
        assertFalse(wait.isFinished());
        visible.set(false);
        wait.execute();
        visible.set(true);
        clock.set(600_000_000L);
        wait.execute();
        clock.set(1_100_000_000L);
        wait.execute();
        assertTrue(wait.isFinished());
        wait.end(false);
        assertEquals(6, updates.get());
        assertEquals(1, stops.get());
    }

    @Test
    public void interruptionStopsTargetingAndReuseResetsWait() {
        AtomicBoolean visible = new AtomicBoolean(true);
        AtomicLong clock = new AtomicLong();
        AtomicInteger stops = new AtomicInteger();
        WaitForVisibleHive wait = new WaitForVisibleHive(visible::get, () -> {},
                stops::incrementAndGet, 500, clock::get);
        wait.initialize();
        wait.execute();
        wait.end(true);
        assertEquals(1, stops.get());
        clock.set(1_000_000_000L);
        wait.initialize();
        wait.execute();
        assertFalse(wait.isFinished());
    }
}
