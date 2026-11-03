package com.metalbenders.strategies;

public class PreloadGardenAndPark extends AutoStrategy {

    public PreloadGardenAndPark(boolean useInsideWaypoints) {
        super(true, true, false, false, useInsideWaypoints);
    }
}
