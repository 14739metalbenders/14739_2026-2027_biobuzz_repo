package com.metalbenders.autostrategies;

public enum AutoStrategyType {
    SHOOT_PRELOAD_AND_PARK(new ShootPreloadAndPark()),
    PARK_ONLY(new ParkOnly());

    private final AutoStrategy strategy;

    AutoStrategyType(AutoStrategy strategy) {
        this.strategy = strategy;
    }

    public AutoStrategy getStrategy() {
        return strategy;
    }
}