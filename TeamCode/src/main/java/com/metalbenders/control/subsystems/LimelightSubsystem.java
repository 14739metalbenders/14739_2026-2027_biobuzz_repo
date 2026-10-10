package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.Constants;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class LimelightSubsystem extends SubsystemBase {

    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private Limelight3A limelight;
    private int totalDetections = 0;
    private int totalDetectionsOfColor = 0;
    private Constants.FieldSide activeHiveCellFieldSide;

    public LimelightSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.allianceColor = allianceColor;
        this.telemetry = telemetry;
        this.limelight = hardwareMap.get(Limelight3A.class, "limelight");
        this.limelight.pipelineSwitch(0);
        if (allianceColor == Constants.AllianceColor.RED) {
            activeHiveCellFieldSide = Constants.FieldSide.SCORE_SIDE;
        } else {
            activeHiveCellFieldSide = Constants.FieldSide.AUDIENCE_SIDE;
        }
    }

    private void processLLResult() {
        LLResult llResult = limelight.getLatestResult();
        if (llResult == null || !llResult.isValid()) {
            return;
        }
        if (llResult.getFiducialResults() != null) {
            llResult.getFiducialResults().forEach(this::processFiducialResult);
        }
    }

    private void processFiducialResult(LLResultTypes.FiducialResult fiducialResult) {
        totalDetections = 0;
        totalDetectionsOfColor = 0;
        if(fiducialResult == null) {
            return;
        }
        totalDetections++;
        AprilTag aprilTag = AprilTag.getAprilTagById(fiducialResult.getFiducialId());
        if(aprilTag != null && aprilTag.getAllianceColor() == allianceColor) {
            totalDetectionsOfColor++;
            //todo: process fiducial result of our alliance color
        }
    }

    public Constants.FieldSide getActiveHiveCellFieldSide() {
        return activeHiveCellFieldSide;
    }

    @Override
    public void periodic() {
        processLLResult();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("Total Detections", totalDetections);
        telemetry.addData("Total Detections of Color", totalDetectionsOfColor);
        telemetry.addLine();
    }
}
