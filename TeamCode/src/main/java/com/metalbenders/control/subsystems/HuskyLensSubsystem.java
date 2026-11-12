package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.metalbenders.constants.Constants;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.AllianceColor.isRed;
import static com.metalbenders.constants.Constants.BLUE_NECTAR_BLOCK_ID;
import static com.metalbenders.constants.Constants.POLLEN_BLOCK_ID;
import static com.metalbenders.constants.Constants.RED_NECTAR_BLOCK_ID;

public class HuskyLensSubsystem extends SubsystemBase {

    private final HuskyLens huskyLens;
    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private Constants.HuskyLensState state;

    public HuskyLensSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        setState();
    }

    public HuskyLens.Block[] getBlocksOfType(Constants.ArtifactType artifactType) {
        switch (artifactType) {
            case POLLEN:
                return huskyLens.blocks(POLLEN_BLOCK_ID);
            case NECTAR:
                if (isRed(allianceColor)) {
                    return huskyLens.blocks(RED_NECTAR_BLOCK_ID);
                } else {
                    return huskyLens.blocks(BLUE_NECTAR_BLOCK_ID);
                }
        }
        return null;
    }

    @Override
    public void periodic() {
        setState();
        telemetry.addLine(this.getClass().getSimpleName());
        telemetry.addLine("--------------------------------");
        telemetry.addData("HuskyLens State", state);
        telemetry.addLine();
    }

    public void teardown() {
        //no teardown needed for HuskyLens
    }

    private void setState() {
        if (!huskyLens.knock()) {
            state = Constants.HuskyLensState.FAILED;
        } else {
            state = Constants.HuskyLensState.READY;
        }
    }

    public boolean isHealthy() {
        return state == Constants.HuskyLensState.READY;
    }
}
