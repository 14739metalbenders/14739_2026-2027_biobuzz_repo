package com.metalbenders.constants;

public enum GameElementType {
    POLLEN("pollen"),
    RED_NECTAR("red_nectar"),
    BLUE_NECTAR("blue_nectar"),
    ROBOT("robot");

    private String className;
    GameElementType(String className) {
        this.className = className;
    }
}
