package com.metalbenders.util;

import com.metalbenders.constants.Constants.AllianceColor;
import com.metalbenders.constants.FieldSide;
import com.pedropathing.math.Pose;

public final class AutonomousStorage {
    private static Handoff pending;

    private AutonomousStorage() {
    }

    public static final class Handoff {
        public final Pose pose;
        public final FieldSide hiveSide;
        private final AllianceColor alliance;

        private Handoff(Pose pose, FieldSide hiveSide, AllianceColor alliance) {
            this.pose = new Pose(pose.x(), pose.y(), pose.heading());
            this.hiveSide = hiveSide;
            this.alliance = alliance;
        }
    }

    public static void save(Pose pose, FieldSide hiveSide, AllianceColor alliance) {
        pending = PoseUtil.isFinite(pose) ? new Handoff(pose, hiveSide, alliance) : null;
    }

    public static Handoff consume(AllianceColor alliance) {
        Handoff handoff = pending;
        pending = null;
        return handoff != null && handoff.alliance == alliance ? handoff : null;
    }
}
