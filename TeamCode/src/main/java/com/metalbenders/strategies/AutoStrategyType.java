package com.metalbenders.strategies;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_HIVE_SWITCH;

public enum AutoStrategyType {
    PRELOAD_AND_PARK(
            PreloadAndPark::new,
            "Shoot preload then park, delays " + MILLIS_TO_WAIT_FOR_HIVE_SWITCH
                    + " millis if starting on the inactive side"),
    PARK_ONLY(
            ParkOnly::new,
            "Go directly to park"),
    PRELOAD_GARDEN_AND_PARK(
            PreloadGardenAndPark::new,
            "Shoot preload, collect and shoot garden, then park"
    );

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