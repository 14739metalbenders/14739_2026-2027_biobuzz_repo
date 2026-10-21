package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants;
import com.metalbenders.opmodes.BaseOpMode;
import com.metalbenders.opmodes.auto.strategies.AudienceSideTestStrategy;
import com.metalbenders.opmodes.auto.strategies.AutoStrategy;
import com.pedropathing.math.Pose;

import static com.metalbenders.constants.FieldLocations.*;

public abstract class BaseAuto extends BaseOpMode {

    @Override
    public Constants.OpModeType getOpModeType() {
        return Constants.OpModeType.AUTO;
    }

    public AutoStrategy getAutoStrategy() {
        return new AudienceSideTestStrategy();
    }

    @Override
    public Pose getInitialPose() {
        return getAutoStrategy().getStartingFieldSide() == Constants.FieldSide.AUDIENCE_SIDE
                ? AUDIENCE_SIDE_START_POSE.getPose(getAllianceColor())
                : SCORE_SIDE_START_POSE.getPose(getAllianceColor());
    }

    public Pose getParkPose() {
        return PARK.getPose(getAllianceColor());
    }

    @Override
    public void initialize() {
        super.initialize();
        getAutoStrategy().initialize(getAllianceColor(), getParkPose(), getSubsystemMap());
    }
}
