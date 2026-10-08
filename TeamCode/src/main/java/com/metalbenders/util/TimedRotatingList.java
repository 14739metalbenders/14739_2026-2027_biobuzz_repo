package com.metalbenders.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-capacity, thread-safe rolling history of values stamped with the time they were added.
 * Once full, adding a new value discards the oldest one.
 *
 * <p>Timestamps use {@link System#currentTimeMillis()}. Since entries are added in time order,
 * {@link #findClosest(long)} uses a binary search (O(log n)).
 */
public class TimedRotatingList<T> {

    public static final class Entry<T> {
        private final long timeMillis;
        private final T value;

        private Entry(long timeMillis, T value) {
            this.timeMillis = timeMillis;
            this.value = value;
        }

        public long getTimeMillis() {
            return timeMillis;
        }

        public T getValue() {
            return value;
        }
    }

    private final Entry<T>[] buffer;
    private final int capacity;
    private int head = 0; // index of oldest entry
    private int size = 0;

    @SuppressWarnings("unchecked")
    public TimedRotatingList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0, was " + capacity);
        }
        this.capacity = capacity;
        this.buffer = (Entry<T>[]) new Entry[capacity];
    }

    /** Adds a value stamped with the current time, evicting the oldest entry if full. */
    public synchronized Entry<T> add(T value) {
        long now = System.currentTimeMillis();
        // Guard against wall-clock going backwards so entries stay sorted for binary search.
        if (size > 0) {
            now = Math.max(now, get(size - 1).timeMillis);
        }
        Entry<T> entry = new Entry<>(now, value);
        if (size == capacity) {
            buffer[head] = entry;
            head = (head + 1) % capacity;
        } else {
            buffer[(head + size) % capacity] = entry;
            size++;
        }
        return entry;
    }

    /** @param index 0 = oldest, size() - 1 = newest */
    public synchronized Entry<T> get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        return buffer[(head + index) % capacity];
    }

    /** @return the newest entry, or null if empty. */
    public synchronized Entry<T> newest() {
        return size == 0 ? null : get(size - 1);
    }

    /** @return the oldest entry, or null if empty. */
    public synchronized Entry<T> oldest() {
        return size == 0 ? null : get(0);
    }

    /** @return the entry whose timestamp is closest to {@code timeMillis}, or null if empty. */
    public synchronized Entry<T> findClosest(long timeMillis) {
        if (size == 0) {
            return null;
        }
        int lo = 0;
        int hi = size - 1;
        // Find the first entry with time >= timeMillis.
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (get(mid).timeMillis < timeMillis) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        Entry<T> candidate = get(lo);
        if (lo > 0) {
            Entry<T> before = get(lo - 1);
            if (Math.abs(before.timeMillis - timeMillis) <= Math.abs(candidate.timeMillis - timeMillis)) {
                return before;
            }
        }
        return candidate;
    }

    /**
     * @return the closest entry if it is within {@code maxDiffMillis} of {@code timeMillis},
     *         otherwise null.
     */
    public synchronized Entry<T> findClosest(long timeMillis, long maxDiffMillis) {
        Entry<T> closest = findClosest(timeMillis);
        if (closest == null || Math.abs(closest.timeMillis - timeMillis) > maxDiffMillis) {
            return null;
        }
        return closest;
    }

    /** Convenience: value of the closest entry within {@code maxDiffMillis}, or null. */
    public synchronized T findClosestValue(long timeMillis, long maxDiffMillis) {
        Entry<T> closest = findClosest(timeMillis, maxDiffMillis);
        return closest == null ? null : closest.value;
    }

    public synchronized int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = null;
        }
        head = 0;
        size = 0;
    }

    /** @return a snapshot copy of entries ordered oldest to newest. */
    public synchronized List<Entry<T>> toList() {
        List<Entry<T>> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add(get(i));
        }
        return copy;
    }
}
