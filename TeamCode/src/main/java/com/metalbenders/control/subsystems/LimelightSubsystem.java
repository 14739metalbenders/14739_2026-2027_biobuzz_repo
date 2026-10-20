package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.Constants;
import com.metalbenders.util.TimedEntity;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

import static com.metalbenders.constants.Constants.FieldSide.AUDIENCE_SIDE;
import static com.metalbenders.constants.Constants.FieldSide.SCORE_SIDE;
import static com.metalbenders.util.MathUtil.calculateAverageTy;

public class LimelightSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private final Limelight3A limelight;

    private Constants.FieldSide activeHiveCellFieldSide;

    private TimedEntity<List<LLResultTypes.FiducialResult>> lastActiveSideDetections =
            new TimedEntity<>(new ArrayList<>());
    private TimedEntity<List<LLResultTypes.FiducialResult>> lastInactiveSideDetections =
            new TimedEntity<>(new ArrayList<>());

    public LimelightSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");

        limelight.pipelineSwitch(0);
        limelight.start();

        activeHiveCellFieldSide = (allianceColor == Constants.AllianceColor.RED) ? SCORE_SIDE : AUDIENCE_SIDE;
    }

    private void processLLResult() {
        LLResult llResult = limelight.getLatestResult();
        if (!isValidResult(llResult)) {
            return;
        }

        List<LLResultTypes.FiducialResult> scoreSideResults = new ArrayList<>();
        List<LLResultTypes.FiducialResult> audienceSideResults = new ArrayList<>();

        for (LLResultTypes.FiducialResult result : llResult.getFiducialResults()) {
            AprilTag aprilTag = AprilTag.getAprilTagById(result.getFiducialId());
            if (aprilTag == null || aprilTag.getAllianceColor() != allianceColor) {
                continue;
            }

            if (aprilTag.getFieldSide() == SCORE_SIDE) {
                scoreSideResults.add(result);
            } else {
                audienceSideResults.add(result);
            }
        }

        setDetections(scoreSideResults, audienceSideResults);
        updateActiveHiveCellFieldSide();
    }

    private boolean isValidResult(LLResult llResult) {
        return llResult != null
                && llResult.isValid()
                && llResult.getFiducialResults() != null
                && !llResult.getFiducialResults().isEmpty();
    }

    private void setDetections(
            List<LLResultTypes.FiducialResult> scoreSideResults,
            List<LLResultTypes.FiducialResult> audienceSideResults) {
        updateDetectionsForSide(SCORE_SIDE, scoreSideResults);
        updateDetectionsForSide(AUDIENCE_SIDE, audienceSideResults);
    }

    private void updateDetectionsForSide(
            Constants.FieldSide side,
            List<LLResultTypes.FiducialResult> detections) {
        if (detections == null || detections.isEmpty()) {
            return;
        }

        if (activeHiveCellFieldSide == side) {
            lastActiveSideDetections = TimedEntity.of(detections);
        } else {
            lastInactiveSideDetections = TimedEntity.of(detections);
        }
    }

    private void updateActiveHiveCellFieldSide() {
        if (Math.abs(lastActiveSideDetections.getTime() - lastInactiveSideDetections.getTime()) > 100) {
            return;
        }

        double averageTyActiveSide = calculateAverageTy(lastActiveSideDetections.getEntity());
        double averageTyInactiveSide = calculateAverageTy(lastInactiveSideDetections.getEntity());

        // y-values are negative; the lower value is physically higher.
        if (averageTyInactiveSide < averageTyActiveSide) {
            switchActiveSide();
        }
    }

    private void switchActiveSide() {
        activeHiveCellFieldSide = (activeHiveCellFieldSide == SCORE_SIDE) ? AUDIENCE_SIDE : SCORE_SIDE;

        TimedEntity<List<LLResultTypes.FiducialResult>> swap = lastActiveSideDetections;
        lastActiveSideDetections = lastInactiveSideDetections;
        lastInactiveSideDetections = swap;
    }

    public Constants.FieldSide getActiveHiveCellFieldSide() {
        return activeHiveCellFieldSide;
    }

    public void setFieldSideOverride(Constants.FieldSide fieldSideOverride) {
        activeHiveCellFieldSide = fieldSideOverride;
    }

    public TimedEntity<List<LLResultTypes.FiducialResult>> getLastActiveSideDetections() {
        return lastActiveSideDetections;
    }

    @Override
    public void periodic() {
        processLLResult();

        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Active Hive Cell Field Side", getActiveHiveCellFieldSide());

        addDetectionTelemetry("ACTIVE Side Detections", lastActiveSideDetections);
        addDetectionTelemetry("INACTIVE Side Detections", lastInactiveSideDetections);

        telemetry.addLine();
    }

    private void addDetectionTelemetry(
            String header,
            TimedEntity<List<LLResultTypes.FiducialResult>> detections) {
        telemetry.addLine(header);
        telemetry.addData("Time Since Last Detection",
                System.currentTimeMillis() - detections.getTime() + " ms");

        for (LLResultTypes.FiducialResult result : detections.getEntity()) {
            telemetry.addLine("\t" + result.getFiducialId());
        }
    }

    public void teardown() {
        limelight.stop();
    }
}