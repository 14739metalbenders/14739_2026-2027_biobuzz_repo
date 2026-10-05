package com.metalbenders.util;

import java.util.List;

public class TimedEntity<T> {

    private final long time;
    private final T entity;

    public TimedEntity(T entity) {
        this.time = System.currentTimeMillis();
        this.entity = entity;
    }

    public static <T> TimedEntity<T> of (T entity) {
        return new TimedEntity<>(entity);
    }

    public static <T> T findNearestEntity(
            List<TimedEntity<T>> timedEntities, long time, int maxTimeDiffInMillis) {
        T closest = null;
        long minDiff = maxTimeDiffInMillis;

        for (TimedEntity<T> timedEntity : timedEntities) {
            long diff = Math.abs(timedEntity.time - time);
            if (diff <= maxTimeDiffInMillis && diff < minDiff) {
                minDiff = diff;
                closest = timedEntity.entity;
            }
        }
        return closest;
    }
}
