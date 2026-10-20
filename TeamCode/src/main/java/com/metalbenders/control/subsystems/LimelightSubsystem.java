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
        this.allianceColor = allianceColor;
        this.telemetry = telemetry;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");
        this.limelight.pipelineSwitch(0);
        this.limelight.start();
        this.activeHiveCellFieldSide =
                allianceColor == Constants.AllianceColor.RED
                        ? SCORE_SIDE
                        : AUDIENCE_SIDE;
    }

    private void processLLResult() {
        LLResult llResult = limelight.getLatestResult();
        if (llResult == null
                || !llResult.isValid()
                || llResult.getFiducialResults() == null
                || llResult.getFiducialResults().isEmpty()) {
            return;
        }
        List<LLResultTypes.FiducialResult> scoreSideResults = new ArrayList<>();
        List<LLResultTypes.FiducialResult> audienceSideResults = new ArrayList<>();
        for (LLResultTypes.FiducialResult fiducialResult : llResult.getFiducialResults()) {
            AprilTag aprilTag = AprilTag.getAprilTagById(fiducialResult.getFiducialId());
            if (aprilTag != null && aprilTag.getAllianceColor() == allianceColor) {
                if (aprilTag.getFieldSide() == SCORE_SIDE) {
                    scoreSideResults.add(fiducialResult);
                } else {
                    audienceSideResults.add(fiducialResult);
                }
            }
        }
        setDetections(scoreSideResults, audienceSideResults);
        updateActiveHiveCellFieldSide();
    }

    private void setDetections(
            List<LLResultTypes.FiducialResult> scoreSideResults,
            List<LLResultTypes.FiducialResult> audienceSideResults) {
        if (scoreSideResults != null && !scoreSideResults.isEmpty()) {
            if (activeHiveCellFieldSide == SCORE_SIDE) {
                lastActiveSideDetections = TimedEntity.of(scoreSideResults);
            } else {
                lastInactiveSideDetections = TimedEntity.of(scoreSideResults);
            }
        }
        if (audienceSideResults != null && !audienceSideResults.isEmpty()) {
            if (activeHiveCellFieldSide == AUDIENCE_SIDE) {
                lastActiveSideDetections = TimedEntity.of(audienceSideResults);
            } else {
                lastInactiveSideDetections = TimedEntity.of(audienceSideResults);
            }
        }
    }

    private void updateActiveHiveCellFieldSide() {
        if (Math.abs(lastActiveSideDetections.getTime() - lastInactiveSideDetections.getTime()) > 100) {
            return;
        }
        double averageTyActiveSide = calculateAverageTy(lastActiveSideDetections.getEntity());
        double averageTyInactiveSide = calculateAverageTy(lastInactiveSideDetections.getEntity());
        //y-values above are negative, so the lower number is higher (i.e. less negative)
        if (averageTyInactiveSide < averageTyActiveSide) {
            switchActiveSide();
        }
    }

    private void switchActiveSide() {
        activeHiveCellFieldSide = activeHiveCellFieldSide == SCORE_SIDE ? AUDIENCE_SIDE : SCORE_SIDE;
        TimedEntity<List<LLResultTypes.FiducialResult>> swap = lastActiveSideDetections;
        lastActiveSideDetections = lastInactiveSideDetections;
        lastInactiveSideDetections = swap;
    }

    private double calculateAverageTy(List<LLResultTypes.FiducialResult> fiducialResults) {
        double sumTy = 0;
        for (LLResultTypes.FiducialResult fiducialResult : fiducialResults) {
            sumTy += fiducialResult.getCameraPoseTargetSpace().getPosition().y;
        }
        return sumTy / fiducialResults.size();
    }

    public Constants.FieldSide getActiveHiveCellFieldSide() {
        return activeHiveCellFieldSide;
    }

    public void setFieldSideOverride(Constants.FieldSide fieldSideOverride) {
        this.activeHiveCellFieldSide = fieldSideOverride;
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

        //ACTIVE
        telemetry.addLine("ACTIVE Side Detections");
        telemetry.addData("Time Since Last Detection",
                System.currentTimeMillis() - lastActiveSideDetections.getTime() + " ms");
        for (LLResultTypes.FiducialResult result : lastActiveSideDetections.getEntity()) {
            telemetry.addLine("\t" + result.getFiducialId());
        }

        //INACTIVE
        telemetry.addLine("INACTIVE Side Detections");
        telemetry.addData("Time Since Last Detection",
                System.currentTimeMillis() - lastInactiveSideDetections.getTime() + " ms");
        telemetry.addLine("INACTIVE - Detected " + allianceColor + "Tags");
        for (LLResultTypes.FiducialResult result : lastInactiveSideDetections.getEntity()) {
            telemetry.addLine("\t" + result.getFiducialId());
        }
        telemetry.addLine();
    }

    public void teardown() {
        limelight.stop();
    }
}
