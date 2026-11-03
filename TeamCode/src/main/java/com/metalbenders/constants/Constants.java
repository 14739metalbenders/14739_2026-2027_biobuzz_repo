package com.metalbenders.constants;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class Constants {

    public enum OpModeType {
        AUTO,
        TELEOP
    }

    public enum AllianceColor {
        RED,
        BLUE
    }

    public enum ArtifactType {
        POLLEN,
        NECTAR
    }

    public enum IntakeState {
        SHOOT(2400),
        INTAKE(1800),
        REVERSE_INTAKE(-1500),
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

    public enum LimelightState {
        READY,
        FAILED
    }

    public enum LidarState {
        CONNECTED,
        FAILED
    }

    public enum IndicatorLedEnum {
        BLACK(0.0),
        RED(0.280),
        ORANGE(0.326),
        YELLOW(0.391),
        GREEN(0.503),
        BLUE(0.625),
        PURPLE(0.725),
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

    public static final Pose DEFAULT_POSE = new Pose(0, 0, Math.toRadians(90));

    /*LAUNCHER CONSTANTS*/
    public static final long LAUNCH_GATE_POSITION_DELAY_MILLI = 125;
    public static final PIDFCoefficients LAUNCHER_VELOCITY_PID = new PIDFCoefficients(150, 0, 0, 15);
    public static final PIDFCoefficients TURRET_VELOCITY_PID = new PIDFCoefficients(23.25, 0.0, 0.8, 7.5);

    /*FLOWER TOOL CONSTANTS*/
    public static final long FLOWER_TOOL_POSITION_DELAY_MILLIS = 200;

    /*HUSKY LENS CONSTANTS*/
    public static final int POLLEN_BLOCK_ID = 1;
    public static final int BLUE_NECTAR_BLOCK_ID = 2;
    public static final int RED_NECTAR_BLOCK_ID = 3;

    /*AUTO CONSTANTS*/
    public static final long AUTO_PARK_OVERRIDE_DELAY_MILLIS = 25000;
    public static final long AUTO_SHOOT_TIMEOUT_MILLIS = 750;
    public static final long HIVE_SWITCH_TIMEOUT_MILLIS = 5000;
    public static final long GARDEN_INTAKE_TIMEOUT_MILLIS = 1000;
    public static final long FLOWER_INTAKE_TIMEOUT_MILLIS = 1000;
    public static final double DISTANCE_TO_TRAVEL_TO_INTAKE_FROM_FLOWER = 20.0;
}
