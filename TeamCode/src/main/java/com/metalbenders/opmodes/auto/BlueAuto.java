package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants.AllianceColor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Blue Auto", group = "Auto")
public class BlueAuto extends BaseAuto {

    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.BLUE;
    }
}
