package com.metalbenders.strategies;

public class PreloadGardenParkSideFlowerAndPark extends AutoStrategy {

    public PreloadGardenParkSideFlowerAndPark(boolean useInsideWaypoints) {
        super(true, true, true, false, useInsideWaypoints);
    }
}
