package com.metalbenders.util;

import com.metalbenders.constants.GameElementType;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;

import java.util.List;

public class GameElementTracker {

    private static final GameElementTracker instance = new GameElementTracker();
    private List<GameElementTrackingEntry> gameElementTrackingEntries;
    public static GameElementTracker getInstance() {
        return instance;
    }

    public void processLLResult(LLResult llResult, Pose currentLocation) {
        if (llResult != null && llResult.isValid()) {
            if (llResult.getDetectorResults() != null && !llResult.getDetectorResults().isEmpty()) {
                llResult.getDetectorResults().forEach(detectorResult -> {
//                    detectorResult.
                });
            } else {
                gameElementTrackingEntries.forEach(gameElementTrackingEntry -> {
                    gameElementTrackingEntry.incrementMissedDetectionCount();
                });
            }
        }
    }

    private static class GameElementTrackingEntry {

        private final int x;
        private final int y;
        private final GameElementType gameElementType;
        private int detectionCount = 0;
        private int missedDetectionCount = 0;

        public GameElementTrackingEntry(int x, int y, GameElementType gameElementType) {
            this.x = x;
            this.y = y;
            this.gameElementType = gameElementType;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public GameElementType getGameElementType() {
            return gameElementType;
        }

        public int getDetectionCount() {
            return detectionCount;
        }

        public void incrementDetectionCount() {
            detectionCount++;
        }

        public int getMissedDetectionCount() {
            return missedDetectionCount;
        }

        public void incrementMissedDetectionCount() {
            missedDetectionCount++;
        }
    }
}
