package com.metalbenders.util;

import java.util.ArrayList;
import java.util.List;

public class RotatingBuffer<T> {
    private final T[] buffer;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public RotatingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
    }

    public synchronized void put(T item) {
        if (size == capacity) {
            // Buffer is full: advance head to let the oldest item be overwritten
            head = (head + 1) % capacity;
            size--;
        }

        buffer[tail] = item;
        tail = (tail + 1) % capacity;
        size++;
    }

    /**
     * Retrieves an entry relative to the oldest active item without removing it.
     * @param index 0 for the oldest item, (size - 1) for the newest item.
     */
    public synchronized T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        // Calculate internal array position relative to head
        int internalIndex = (head + index) % capacity;
        return buffer[internalIndex];
    }

    /**
     * Returns a full copy of the current buffer items in order from oldest to newest.
     */
    public synchronized List<T> toList() {
        List<T> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add(buffer[(head + i) % capacity]);
        }
        return copy;
    }
}
