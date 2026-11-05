package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class LightingSubsystem extends SubsystemBase {

    private static final double BLINK_INTERVAL_SECONDS = 2.0;

    private final Servo lightingServo;
    private final Telemetry telemetry;
    private final ElapsedTime animationTimer = new ElapsedTime();
    private Constants.IndicatorLedEnum currentColor;
    private Constants.IndicatorAnimationEnum currentAnimation = Constants.IndicatorAnimationEnum.SOLID;

    public LightingSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.lightingServo = hardwareMap.get(Servo.class, "signalLed");
        this.telemetry = telemetry;
        setCurrentColor(Constants.IndicatorLedEnum.valueOf(allianceColor.name()));
        setCurrentAnimation(Constants.IndicatorAnimationEnum.SOLID);
    }

    public void setCurrentColor(Constants.IndicatorLedEnum color) {
        currentColor = color;
        updateColor();
    }

    /** Starts a new animation with the selected color; BLINK alternates every two seconds. */
    public void setCurrentAnimation(Constants.IndicatorAnimationEnum animation) {
        if (currentAnimation != animation) {
            currentAnimation = animation;
            animationTimer.reset();
        }
        updateColor();
    }

    private void updateColor() {
        boolean showBlack = currentAnimation == Constants.IndicatorAnimationEnum.BLINK
                && (long) (animationTimer.seconds() / BLINK_INTERVAL_SECONDS) % 2 == 1;
        Constants.IndicatorLedEnum displayedColor = showBlack
                ? Constants.IndicatorLedEnum.BLACK : currentColor;
        lightingServo.setPosition(displayedColor.getLedValue());
    }

    @Override
    public void periodic() {
        updateColor();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Current Color", currentColor.name());
        telemetry.addData("Current Animation", currentAnimation.name());
        telemetry.addLine();
    }
}
