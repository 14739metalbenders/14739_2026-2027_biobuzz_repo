package com.metalbenders.util;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;

import java.util.List;

public class LimelightResultsProcessor {

    public static void processLLResult(LLResult llResult, List<TimedEntity<Pose>> timedPoses) {
        if (llResult == null || !llResult.isValid()) {
            return;
        }
        if (llResult.getFiducialResults() != null && !llResult.getFiducialResults().isEmpty()) {
            llResult.getFiducialResults().forEach(fiducialResult -> {
                long llResultTime = Math.round(llResult.getTimestamp() * 1000.0);
                Pose pose = TimedEntity.findNearestEntity(timedPoses, llResultTime, 50);
                if (pose != null) {
                    processFiducialResult(fiducialResult, pose);
                }
            });
        }
    }

    public static void processFiducialResult(
            LLResultTypes.FiducialResult fiducialResult, Pose robotLocation) {

    }
}
