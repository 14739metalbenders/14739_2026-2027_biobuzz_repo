package com.metalbenders.control.subsystems;

import com.arcrobotics.ftclib.command.Subsystem;
import com.metalbenders.constants.Constants;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.*;

public class HuskyLensSubsystem implements Subsystem {



    private final HuskyLens huskyLens;
    private final Telemetry telemetry;
    private final Constants.AllianceColor allianceColor;
    private final Constants.HuskyLensState state;

    public HuskyLensSubsystem(HardwareMap hardwareMap, Telemetry telemetry, Constants.AllianceColor allianceColor) {
        this.huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        this.telemetry = telemetry;
        this.allianceColor = allianceColor;
        if (!huskyLens.knock()) {
            state = Constants.HuskyLensState.FAILED;
        } else {
            state = Constants.HuskyLensState.READY;
        }
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
}
