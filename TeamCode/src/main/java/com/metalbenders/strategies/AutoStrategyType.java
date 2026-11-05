package com.metalbenders.strategies;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;

public enum AutoStrategyType {

    PARK_ONLY(
            ParkOnly::new,
            "Go directly to park"),
    PRELOAD_AND_PARK(
            PreloadAndPark::new,
            "Shoot preload then park, delays " + MILLIS_TO_WAIT_FOR_HIVE_SWITCH
                    + " millis if starting on the inactive side"),
    PRELOAD_GARDEN_AND_PARK_INSIDE(
            () -> new PreloadGardenAndPark(true),
            "Shoot preload, collect and shoot garden, then park. Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_AND_PARK_OUTSIDE(
            () -> new PreloadGardenAndPark(false),
            "Shoot preload, collect and shoot garden, then park. Uses outside waypoints when crossing field."),
    PRELOAD_GARDEN_NON_PARK_SIDE_FLOWER_AND_PARK_INSIDE(
            () -> new PreloadGardenOtherFlowerAndPark(true),
            "Shoot preload, collect and shoot garden, collect and shoot non-park side flower, then park. Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_NON_PARK_SIDE_FLOWER_AND_PARK_OUTSIDE(
            () -> new PreloadGardenOtherFlowerAndPark(false),
            "Shoot preload, collect and shoot garden, collect and shoot non-park side flower, then park. Uses outside waypoints when crossing field.");

    private final Supplier<AutoStrategy> strategySupplier;
    private final String description;

    AutoStrategyType(Supplier<AutoStrategy> strategySupplier, String description) {
        this.strategySupplier = strategySupplier;
        this.description = description;
    }

    public AutoStrategy getStrategy() {
        return strategySupplier.get();
    }

    public String getDescription() {
        return description;
    }
}