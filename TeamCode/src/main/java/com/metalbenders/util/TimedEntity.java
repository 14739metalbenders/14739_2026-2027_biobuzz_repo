package com.metalbenders.util;

public class TimedEntity<T> {

    private final long time;
    private final T entity;

    public TimedEntity(T entity) {
        this.time = System.currentTimeMillis();
        this.entity = entity;
    }

    public long getTime() {
        return time;
    }

    public T getEntity() {
        return entity;
    }

    public static <T> TimedEntity<T> of (T entity) {
        return new TimedEntity<>(entity);
    }
}