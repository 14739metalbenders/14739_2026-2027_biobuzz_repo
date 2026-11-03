package com.metalbenders.strategies;

import java.util.function.Supplier;

public enum AutoStrategyType {

    PARK_ONLY(
            ParkOnly::new,
            "Go directly to park"),
    PRELOAD_AND_PARK(
            PreloadAndPark::new,
            "Shoot preload then park."),
    PRELOAD_GARDEN_AND_PARK_INSIDE(
            () -> new PreloadGardenAndPark(true),
            "Shoot preload, collect and shoot garden, then park. Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_AND_PARK_OUTSIDE(
            () -> new PreloadGardenAndPark(false),
            "Shoot preload, collect and shoot garden, then park. Uses outside waypoints when crossing field."),
    PRELOAD_GARDEN_PARK_SIDE_FLOWER_AND_PARK_INSIDE(
            () -> new PreloadGardenParkSideFlowerAndPark(true),
            "Shoot preload, collect and shoot garden, collect and shoot park side flower, then park. " +
                    "Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_PARK_SIDE_FLOWER_AND_PARK_OUTSIDE(
            () -> new PreloadGardenParkSideFlowerAndPark(false),
            "Shoot preload, collect and shoot garden, collect and shoot park side flower, then park. " +
                    "Uses outside waypoints when crossing field."),
    PRELOAD_GARDEN_OTHER_FLOWER_AND_PARK_INSIDE(
            () -> new PreloadGardenOtherFlowerAndPark(true),
            "Shoot preload, collect and shoot garden, then park. Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_OTHER_FLOWER_AND_PARK_OUTSIDE(
            () -> new PreloadGardenOtherFlowerAndPark(false),
            "Shoot preload, collect and shoot garden, then park. Uses outside waypoints when crossing field."),
    PRELOAD_GARDEN_BOTH_FLOWERS_AND_PARK_INSIDE(
            () -> new PreloadGardenBothFlowersAndPark(true),
            "Shoot preload, collect and shoot garden, collect and shoot other flower, collect and shoot " +
                    "park side flower, then park. Uses inside waypoints when crossing field."),
    PRELOAD_GARDEN_BOTH_FLOWERS_AND_PARK_OUTSIDE(
            () -> new PreloadGardenBothFlowersAndPark(false),
            "Shoot preload, collect and shoot garden, collect and shoot other flower, collect and shoot " +
                    "park side flower, then park. Uses outside waypoints when crossing field.");

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