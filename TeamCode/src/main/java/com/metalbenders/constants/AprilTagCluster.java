package com.metalbenders.constants;

import java.util.List;

public enum AprilTagCluster {

    RED_SCORE_SIDE(30,31,32,33),
    RED_AUDIENCE_SIDE(37,36,35,34),
    BLUE_SCORE_SIDE(45,44,43,42),
    BLUE_AUDIENCE_SIDE(38,39,40,41);

    private final int insideId;
    private final int insideCenterId;
    private final int outsideCenterId;
    private final int outsideId;

    AprilTagCluster(int insideId, int insideCenterId, int outsideCenterId, int outsideId) {
        this.insideId = insideId;
        this.insideCenterId = insideCenterId;
        this.outsideCenterId = outsideCenterId;
        this.outsideId = outsideId;
    }

    public int getInsideId() {
        return insideId;
    }

    public int getInsideCenterId() {
        return insideCenterId;
    }

    public int getOutsideCenterId() {
        return outsideCenterId;
    }

    public int getOutsideId() {
        return outsideId;
    }

    private List<Integer> getAllIds() {
        return List.of(insideId, insideCenterId, outsideCenterId, outsideId);
    }

    private boolean containsId(int id) {
        return getAllIds().contains(id);
    }

    public static AprilTagCluster getCluster(int id) {
        for (AprilTagCluster cluster : AprilTagCluster.values()) {
            if (cluster.containsId(id)) {
                return cluster;
            }
        }
        return null;
    }
}
