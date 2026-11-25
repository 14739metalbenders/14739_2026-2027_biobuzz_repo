package com.metalbenders.constants;

import com.acmerobotics.dashboard.config.Config;

/** Dashboard calibration: viewing angles in degrees, lateral offsets in inches. */
@Config
public final class HiveAimOffsetConfig {
    public static volatile boolean USE_OFF_CENTER_AIMING = false;
    public static Table RED_SCORING = new Table();
    public static Table RED_AUDIENCE = new Table();
    public static Table BLUE_SCORING = new Table();
    public static Table BLUE_AUDIENCE = new Table();

    private HiveAimOffsetConfig() {
    }

    public static final class Table {
        public volatile double angle1 = -45;
        public volatile double offset1 = 0;
        public volatile double angle2 = 0;
        public volatile double offset2 = 0;
        public volatile double angle3 = 45;
        public volatile double offset3 = 0;
    }

    public static Table forCell(Constants.AllianceColor alliance, FieldSide side) {
        if (alliance == Constants.AllianceColor.RED) {
            return side == FieldSide.SCORING ? RED_SCORING : side == FieldSide.AUDIENCE ? RED_AUDIENCE : null;
        }
        if (alliance == Constants.AllianceColor.BLUE) {
            return side == FieldSide.SCORING ? BLUE_SCORING : side == FieldSide.AUDIENCE ? BLUE_AUDIENCE : null;
        }
        return null;
    }
}
