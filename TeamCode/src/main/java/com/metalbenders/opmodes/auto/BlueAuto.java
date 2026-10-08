package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants.AllianceColor;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Blue Auto", group = "Auto")
public class BlueAuto extends BaseAuto {

    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.BLUE;
    }

    @Override
    public Pose getInitialPose() {
        return null;
    }
}
