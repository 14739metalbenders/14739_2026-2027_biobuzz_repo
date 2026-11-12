package com.metalbenders.util;

public class TimedEntity<T> {
    private final T entity;
    private final long timestamp;

    public TimedEntity(T entity) {
        this.entity = entity;
        this.timestamp = System.currentTimeMillis();
    }

    public T get() {
        return entity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static <T> TimedEntity<T> of(T entity) {
        return new TimedEntity<>(entity);
    }
}
