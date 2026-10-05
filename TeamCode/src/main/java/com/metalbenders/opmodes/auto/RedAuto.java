package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants.AllianceColor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Red Auto", group = "Auto")
public class RedAuto extends BaseAuto {

    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.RED;
    }
}
