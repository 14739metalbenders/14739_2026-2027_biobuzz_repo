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

    public enum FieldSide {
        SCORE_SIDE,
        AUDIENCE_SIDE;

        public static FieldSide getOppositeSide(FieldSide fieldSide) {
            return fieldSide == SCORE_SIDE ? AUDIENCE_SIDE : SCORE_SIDE;
        }
    }

    public enum ArtifactType {
        POLLEN,
        NECTAR
    }

    public enum IntakeState {
        SHOOT(2000),
        INTAKE(2300),
        REVERSE_INTAKE(-2300),
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
        OPEN(0.52),
        CLOSED(0.75),
        KICK(0.08);

        private final double position;

        LaunchGateState(double position) {
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

    public static final Pose DEFAULT_POSE = new Pose(0, 0, Math.toRadians(90));

    /*LAUNCHER CONSTANTS*/
    public static final long LAUNCH_GATE_POSITION_DELAY_MILLI = 125;
    public static final double MAX_TURRET_ROTATION_DEGREES = 120.0;
    public static final PIDFCoefficients LAUNCHER_VELOCITY_PID = new PIDFCoefficients(150, 0, 0, 15);
    public static final PIDFCoefficients TURRET_VELOCITY_PID = new PIDFCoefficients(23.25, 0.0, 0.8, 7.5);

    /*HUSKY LENS CONSTANTS*/
    public static final int POLLEN_BLOCK_ID = 1;
    public static final int BLUE_NECTAR_BLOCK_ID = 2;
    public static final int RED_NECTAR_BLOCK_ID = 3;

    /*AUTO CONSTANTS*/
    public static final long MILLIS_TO_WAIT_DURING_AUTO_SHOOT = 750;
    public static final long MILLIS_TO_WAIT_FOR_HIVE_SWITCH = 5000;
}
