package com.metalbenders.util;

import java.util.ArrayList;
import java.util.List;

public class RotatingBuffer<T> {
    private final T[] buffer;
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
        this.buffer = (T[]) new Object[capacity];
    }

    public synchronized void put(T item) {
        buffer[tail] = item;
        tail = (tail + 1) % capacity;

        if (size < capacity) {
            size++;
        } else {
            // The write replaced the oldest item, so move the oldest-item index forward.
            head = (head + 1) % capacity;
        }
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
