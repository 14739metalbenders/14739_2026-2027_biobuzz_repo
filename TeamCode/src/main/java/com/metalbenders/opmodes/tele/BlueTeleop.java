package com.metalbenders.opmodes.tele;

import com.metalbenders.constants.Constants.AllianceColor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Blue Teleop", group = "Teleop")
public class BlueTeleop extends BaseTeleop {

    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.BLUE;
    }
}
