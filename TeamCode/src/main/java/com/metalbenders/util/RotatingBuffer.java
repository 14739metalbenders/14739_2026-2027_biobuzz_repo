package com.metalbenders.util;

import java.util.ArrayList;
import java.util.List;

public class RotatingBuffer<T> {
    private final TimedEntity<T>[] buffer;
    private final int capacity;
    private int head;
    private int tail;
    private int size;

    @SuppressWarnings("unchecked")
    public RotatingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }

        this.capacity = capacity;
        this.buffer = (TimedEntity<T>[]) new TimedEntity<?>[capacity];
    }

    public synchronized void put(T item) {
        buffer[tail] = TimedEntity.of(item);
        tail = (tail + 1) % capacity;

        if (size < capacity) {
            size++;
        } else {
            // The write replaced the oldest item, so move the oldest-item index forward.
            head = (head + 1) % capacity;
        }
    }

    /**
     * Returns a full copy of the timestamped buffer items in order from oldest to newest.
     */
    public synchronized List<TimedEntity<T>> toList() {
        List<TimedEntity<T>> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add(buffer[(head + i) % capacity]);
        }
        return copy;
    }

    /**
     * Returns the nearest entity within the inclusive time tolerance, or null if none matches.
     * Equal distances favor the older entry. Assumes system time never moves backward.
     */
    public synchronized T findNearestEntity(long time, int maxTimeDiffInMillis) {
        if (maxTimeDiffInMillis < 0) {
            throw new IllegalArgumentException("Maximum time difference must not be negative");
        }

        T closest = null;
        long minDiff = (long) maxTimeDiffInMillis + 1;
        for (int i = 0; i < size; i++) {
            TimedEntity<T> timedEntity = buffer[(head + i) % capacity];
            long diff = timedEntity.time >= time
                    ? timedEntity.time - time : time - timedEntity.time;
            // Negative differences indicate overflow for timestamps outside the supported tolerance.
            if (diff >= 0 && diff < minDiff) {
                minDiff = diff;
                closest = timedEntity.entity;
            }
            if (timedEntity.time >= time) {
                break;
            }
        }
        return closest;
    }

    public static class TimedEntity<T> {

        private final long time;
        private final T entity;

        public TimedEntity(T entity) {
            this.time = System.currentTimeMillis();
            this.entity = entity;
        }

        public static <T> TimedEntity<T> of (T entity) {
            return new TimedEntity<>(entity);
        }
    }
}
