package com.metalbenders.constants;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class Constants {

    public static final Pose DEFAULT_POSE = new Pose(0, 0, Math.toRadians(90));
    public static final double CONVERT_METERS_TO_INCHES = 39.3701;
    /*LAUNCHER CONSTANTS*/
    public static final long LAUNCH_GATE_POSITION_DELAY_MILLI = 125;
    public static final PIDFCoefficients LAUNCHER_VELOCITY_PID = new PIDFCoefficients(115, 0, 0, 15);
    public static final long TIME_TO_WAIT_TO_SHOOT_BEFORE_KICK = 600;
    /*HUSKY LENS CONSTANTS*/
    public static final int POLLEN_BLOCK_ID = 1;
    public static final int BLUE_NECTAR_BLOCK_ID = 2;
    public static final int RED_NECTAR_BLOCK_ID = 3;
    /*AUTO CONSTANTS*/
    public static final long MILLIS_TO_WAIT_FOR_HIVE_SWITCH = 5000;
    public static final long MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL = 2000;
    public static final long MILLIS_TO_WAIT_FOR_GARDEN_INTAKE = 1000;
    public static final long MILLIS_TO_WAIT_FOR_FLOWER_INTAKE = 1000;
    public static final long MILLIS_TO_WAIT_FOR_FLOWER_TRAVEL = 2000;
    public static final long FLOWER_TOOL_POSITION_DELAY_MILLIS = 200;
    public static final double FLOWER_COLLECTION_MAX_SPEED = 1.0;
    public static final double FLOWER_DISTANCE_TO_BACKUP_WHEN_COLLECTING = 5.0;
    public static final double FLOWER_DISTANCE_TO_BACKUP_WHEN_FINISHED_COLLECTING = 10.0;
    public static final double FLOWER_COLLECT_OSCILLATION_HEADING_VARIATION = Math.toRadians(8.0);
    public static final long MILLIS_TO_WAIT_FOR_OSCILLATION_ON_FLOWER = 200;
    public static final double AUTO_SPEED_MULTIPLIER = 0.75;
    public static final long AUTO_PARK_OVERRIDE_DELAY_MILLIS = 25000;
    public static final long MAX_TIME_TO_COMPARE_TAG_DETECTIONS = 500;
    public static final double MIN_HEIGHT_DIFFERENCE_TO_DETECT_TRANSITION = 0.000254; //0.5 inches in meters
    public static final double MIN_ACTIVE_HIVE_CELL_HEIGHT = 1.2; //meters
    public static final long DEFAULT_TIME_TO_WAIT_FOR_TRANSITION_STATE_MILLIS = 300;
    public static final long MAX_TIME_TO_WAIT_FOR_TRANSITION_WHEN_STARTING_INACTIVE = 2000;

    public enum OpModeType {
        AUTO,
        TELEOP
    }

    public enum AllianceColor {
        RED,
        BLUE;

        public static boolean isRed(Constants.AllianceColor allianceColor) {
            return allianceColor == Constants.AllianceColor.RED;
        }

        public static boolean isBlue(Constants.AllianceColor allianceColor) {
            return allianceColor == Constants.AllianceColor.BLUE;
        }
    }

    public enum ArtifactType {
        POLLEN,
        NECTAR
    }

    public enum IntakeState {
        SHOOT(0.6),
        INTAKE(0.9),
        REVERSE(-0.8),
        STOP(0);

        private final double targetVelocity;

        IntakeState(double targetVelocity) {
            this.targetVelocity = targetVelocity;
        }

        public double getTargetVelocity() {
            return targetVelocity;
        }
    }

    public enum LaunchGateState {
        OPEN(0.5),
        CLOSED(0.85),
        KICK(0.12);

        private final double position;

        LaunchGateState(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    public enum FlowerToolState {
        UP(1.0),
        DOWN(0.26);

        private final double position;

        FlowerToolState(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    public enum HuskyLensState {
        READY,
        FAILED
    }

    public enum LidarState {
        CONNECTED,
        FAILED
    }

    public enum LimelightState {
        READY,
        FAILED
    }

    public enum IndicatorLedEnum {
        BLACK(0.0),
        RED(0.277),
        ORANGE(0.333),
        YELLOW(0.388),
        SAGE(0.444),
        GREEN(0.5),
        AZURE(0.555),
        BLUE(0.611),
        INDIGO(0.666),
        VIOLET(0.722),
        WHITE(1.0);

        private final double value;

        IndicatorLedEnum(double value) {
            this.value = value;
        }

        public double getLedValue() {
            return value;
        }
    }

    public enum IndicatorAnimationEnum {
        BLINK,
        SOLID
    }

    public enum AutoChallengeType {
        PRELOAD,
        GARDEN,
        PARK_SIDE_FLOWER,
        HIVE_SIDE_FLOWER,
        PARK
    }

    public enum WaypointSeriesType {
        OUTER,
        INNER
    }

    public enum HiveState {
        TRANSITION_TO_AUDIENCE(FieldSide.AUDIENCE, true, false),
        TRANSITION_TO_SCORING(FieldSide.SCORING, true, false),
        ACTIVE_AUDIENCE(FieldSide.AUDIENCE, false, true),
        ACTIVE_SCORING(FieldSide.SCORING, false, true);

        private final boolean transitioning;
        private final boolean active;
        private final FieldSide fieldSide;

        HiveState(FieldSide fieldSide, boolean transitioning, boolean active) {
            this.fieldSide = fieldSide;
            this.transitioning = transitioning;
            this.active = active;
        }

        public FieldSide getFieldSide() {
            return fieldSide;
        }

        public boolean isTransitioning() {
            return transitioning;
        }

        public boolean isActive() {
            return active;
        }
    }
}
