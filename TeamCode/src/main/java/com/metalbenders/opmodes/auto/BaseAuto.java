package com.metalbenders.opmodes.auto;

import com.metalbenders.constants.Constants;
import com.metalbenders.opmodes.BaseOpMode;

public abstract class BaseAuto extends BaseOpMode {

    @Override
    public Constants.OpModeType getOpModeType() {
        return Constants.OpModeType.AUTO;
    }
}
