package com.metalbenders.opmodes.tele;

import com.metalbenders.constants.Constants.AllianceColor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Red Teleop", group = "Teleop")
public class RedTeleop extends BaseTeleop {

    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.RED;
    }
}
