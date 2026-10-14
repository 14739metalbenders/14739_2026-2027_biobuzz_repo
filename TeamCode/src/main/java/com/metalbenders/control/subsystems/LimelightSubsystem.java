package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.Constants;
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

    public LimelightSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.allianceColor = allianceColor;
        this.telemetry = telemetry;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");
        this.limelight.pipelineSwitch(0);
        if (allianceColor == Constants.AllianceColor.RED) {
            this.activeHiveCellFieldSide = SCORE_SIDE;
        } else {
            this.activeHiveCellFieldSide = AUDIENCE_SIDE;
        }
    }

    private void processLLResult() {
        LLResult llResult = limelight.getLatestResult();
        if (llResult == null
                || !llResult.isValid()
                || llResult.getFiducialResults() == null
                || llResult.getFiducialResults().isEmpty()) {
            return;
        }
        if (llResult.getFiducialResults() != null && !llResult.getFiducialResults().isEmpty()) {
            processFiducialResults(llResult.getFiducialResults());
        }
    }

    private void processFiducialResults(List<LLResultTypes.FiducialResult> fiducialResults) {
        List<LLResultTypes.FiducialResult> scoreSideResults = new ArrayList<>();
        List<LLResultTypes.FiducialResult> audienceSideResults = new ArrayList<>();
        //Group the results by cluster, ignoring results for the other alliance color.
        for (LLResultTypes.FiducialResult fiducialResult : fiducialResults) {
            AprilTag aprilTag = AprilTag.getAprilTagById(fiducialResult.getFiducialId());
            if (aprilTag != null && aprilTag.getAllianceColor() == allianceColor) {
                if(aprilTag.getFieldSide() == SCORE_SIDE) {
                    scoreSideResults.add(fiducialResult);
                } else if (aprilTag.getFieldSide() == AUDIENCE_SIDE) {
                    audienceSideResults.add(fiducialResult);
                }
            }
        }
        updateActiveHiveCellFieldSide(scoreSideResults, audienceSideResults);
        if (activeHiveCellFieldSide == SCORE_SIDE) {
            processActiveSideResults(scoreSideResults);
        } else {
            processActiveSideResults(audienceSideResults);
        }
    }

    private void processActiveSideResults(List<LLResultTypes.FiducialResult> fiducialResults) {
        for (LLResultTypes.FiducialResult fiducialResult : fiducialResults) {
            //TODO implement targeting logic
        }
    }

    private void updateActiveHiveCellFieldSide(
            List<LLResultTypes.FiducialResult> scoreSideResults,
            List<LLResultTypes.FiducialResult> audienceSideResults) {
        if (scoreSideResults.isEmpty() || audienceSideResults.isEmpty()) {
            return;
        }
        double averageTyScoreSide = calculateAverageTy(scoreSideResults);
        double averageTyAudienceSide = calculateAverageTy(audienceSideResults);
        if (averageTyScoreSide > averageTyAudienceSide) {
            activeHiveCellFieldSide = SCORE_SIDE;
        } else {
            activeHiveCellFieldSide = AUDIENCE_SIDE;
        }
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

    @Override
    public void periodic() {
        processLLResult();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Active Hive Cell Field Side", activeHiveCellFieldSide);
        telemetry.addLine();
    }
}
