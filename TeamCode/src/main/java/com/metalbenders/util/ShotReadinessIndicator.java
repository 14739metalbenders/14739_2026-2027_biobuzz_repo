package com.metalbenders.util;

public final class ShotReadinessIndicator {
    private static final double VELOCITY_TOLERANCE = 20.0;
    private long lastShotSequence;
    private boolean waitingForRecovery;

    public boolean update(long shotSequence, boolean shooting, boolean hasTarget,
                          double actualVelocity, double targetVelocity,
                          double aimErrorDegrees, double aimDeadbandDegrees) {
        if (shotSequence != lastShotSequence) {
            lastShotSequence = shotSequence;
            waitingForRecovery = true;
        }
        boolean ready = hasTarget
                && Double.isFinite(actualVelocity) && Double.isFinite(targetVelocity)
                && targetVelocity > 0
                && Math.abs(actualVelocity - targetVelocity) <= VELOCITY_TOLERANCE
                && Double.isFinite(aimErrorDegrees) && Double.isFinite(aimDeadbandDegrees)
                && aimDeadbandDegrees > 0 && Math.abs(aimErrorDegrees) < aimDeadbandDegrees;
        if (shooting) {
            return false;
        }
        if (waitingForRecovery) {
            if (!ready) {
                waitingForRecovery = false;
            }
            return false;
        }
        return ready;
    }
}
