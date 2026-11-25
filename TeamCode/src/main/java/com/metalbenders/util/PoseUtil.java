package com.metalbenders.util;

import com.pedropathing.math.Pose;

public final class PoseUtil {
    private PoseUtil() {
    }

    public static boolean isFinite(Pose pose) {
        return pose != null && Double.isFinite(pose.x()) && Double.isFinite(pose.y())
                && Double.isFinite(pose.heading());
    }
}
