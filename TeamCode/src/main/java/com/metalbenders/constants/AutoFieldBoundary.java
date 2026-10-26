package com.metalbenders.constants;

public enum AutoFieldBoundary {

    //These ranges may need to be tweaked so that we aren't going over the middle of the field with any part of the bot
    RED_FIELD_BOUNDARY(Constants.AllianceColor.RED, 0, 72, 0, 144),
    BLUE_FIELD_BOUNDARY(Constants.AllianceColor.BLUE, 72, 144, 0, 144);

    private final Constants.AllianceColor allianceColor;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    AutoFieldBoundary(Constants.AllianceColor allianceColor, double minX, double maxX, double minY, double maxY) {
        this.allianceColor = allianceColor;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public static AutoFieldBoundary getByAllianceColor(Constants.AllianceColor allianceColor) {
        for (AutoFieldBoundary range : AutoFieldBoundary.values()) {
            if (range.allianceColor == allianceColor) {
                return range;
            }
        }
        return null;
    }
}
