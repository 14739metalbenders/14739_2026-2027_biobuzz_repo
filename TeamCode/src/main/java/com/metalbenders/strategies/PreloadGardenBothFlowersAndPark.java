package com.metalbenders.strategies;

public class PreloadGardenBothFlowersAndPark extends AutoStrategy {

    public PreloadGardenBothFlowersAndPark(boolean useInsideWaypoints) {
        super(true, true, true, true, useInsideWaypoints);
    }

}
