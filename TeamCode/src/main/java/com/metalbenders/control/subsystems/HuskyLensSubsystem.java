package com.metalbenders.control.subsystems;

import com.metalbenders.constants.Constants;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class HuskyLensSubsystem extends CloseableSubsystem {

    private enum HuskyLensState {
        READY,
        FAILED
    }

    private final HuskyLens huskyLens;
    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private final HuskyLensState state;
    private static final int POLLEN_BLOCK_ID = 1;
    private static final int BLUE_NECTAR_BLOCK_ID = 2;
    private static final int RED_NECTAR_BLOCK_ID = 3;

    public HuskyLensSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        if (!huskyLens.knock()) {
            state = HuskyLensState.FAILED;
        } else {
            state = HuskyLensState.READY;
        }
    }

    public HuskyLens.Block[] getAllBlocks() {
        return huskyLens.blocks();
    }

    public HuskyLens.Block[] getBlocksOfType(Constants.ArtifactType artifactType) {
        switch (artifactType) {
            case POLLEN:
                return huskyLens.blocks(POLLEN_BLOCK_ID);
            case NECTAR:
                if(allianceColor == Constants.AllianceColor.RED) {
                    return huskyLens.blocks(RED_NECTAR_BLOCK_ID);
                } else {
                    return huskyLens.blocks(BLUE_NECTAR_BLOCK_ID);
                }
        }
        return null;
    }

    @Override
    public void periodic() {
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("HuskyLens State", state);
        telemetry.addLine();
    }

    @Override
    public void close(Constants.OpModeType opModeType) {
        // No specific teardown needed for HuskyLens
    }
}
