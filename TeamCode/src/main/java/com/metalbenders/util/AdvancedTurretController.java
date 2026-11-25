package com.metalbenders.util;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.robotcore.util.Range;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Computes turret angle and launcher speed/power for BioBuzz hive scoring
 * directly from a Pedro Pathing Follower, a Limelight3A LLResult, and the
 * turret encoder angle.
 */
public class AdvancedTurretController {

    public static final double MIN_TURRET_DEG = -70.0;
    public static final double MAX_TURRET_DEG = 70.0;

    public enum Alliance {
        RED, BLUE
    }

    public static class Vec2 {
        public final double x;
        public final double y;

        public Vec2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vec2 add(Vec2 o) { return new Vec2(x + o.x, y + o.y); }
        public Vec2 sub(Vec2 o) { return new Vec2(x - o.x, y - o.y); }
        public Vec2 scale(double s) { return new Vec2(x * s, y * s); }
        public double norm() { return Math.hypot(x, y); }
        public double dot(Vec2 o) { return x * o.x + y * o.y; }

        public Vec2 rotate(double rad) {
            double c = Math.cos(rad);
            double s = Math.sin(rad);
            return new Vec2(x * c - y * s, x * s + y * c);
        }
    }

    public static class HiveCell {
        public final String id;
        public final String hiveId;
        public final Alliance alliance;
        public final Set<Integer> upwardTagIds; // bottom tags visible when cell is UP
        public final Set<Integer> tippedTagIds; // bottom tags visible when cell is TIPPED DOWN
        public final Vec2 openingFieldPos;
        public final Vec2 tagToOpeningOffset;
        public boolean upwardFacing;
        public int consecutiveMisses;

        public HiveCell(
                String id,
                String hiveId,
                Alliance alliance,
                Set<Integer> upwardTagIds,
                Set<Integer> tippedTagIds,
                Vec2 openingFieldPos,
                Vec2 tagToOpeningOffset,
                boolean initialUpwardFacing) {
            this.id = id;
            this.hiveId = hiveId;
            this.alliance = alliance;
            this.upwardTagIds = new HashSet<>(upwardTagIds);
            this.tippedTagIds = new HashSet<>(tippedTagIds);
            this.openingFieldPos = openingFieldPos;
            this.tagToOpeningOffset = tagToOpeningOffset;
            this.upwardFacing = initialUpwardFacing;
            this.consecutiveMisses = 0;
        }

        public boolean isUpwardTag(int tagId) {
            return upwardTagIds.contains(tagId);
        }

        public boolean isTippedTag(int tagId) {
            return tippedTagIds.contains(tagId);
        }

        public boolean matchesAnyTag(int tagId) {
            return upwardTagIds.contains(tagId) || tippedTagIds.contains(tagId);
        }
    }

    public static interface LaunchSpeedModel {
        double outputForDistanceInches(double distanceInches);
        double horizontalBallSpeedIps(double launcherOutput);
    }

    public static class AimSolution {
        public final HiveCell selectedCell;
        public final double commandedTurretDeg;
        public final double rawDesiredTurretDeg;
        public final double effectiveDistanceInches;
        public final double radialSpeedIps;
        public final double launcherOutput;
        public final boolean withinTurretRange;
        public final boolean hasValidOwnAllianceCell;
        public final boolean usedAprilTagTrim;
        public final boolean tipStateChangedThisCycle;

        public AimSolution(
                HiveCell selectedCell,
                double commandedTurretDeg,
                double rawDesiredTurretDeg,
                double effectiveDistanceInches,
                double radialSpeedIps,
                double launcherOutput,
                boolean withinTurretRange,
                boolean hasValidOwnAllianceCell,
                boolean usedAprilTagTrim,
                boolean tipStateChangedThisCycle) {
            this.selectedCell = selectedCell;
            this.commandedTurretDeg = commandedTurretDeg;
            this.rawDesiredTurretDeg = rawDesiredTurretDeg;
            this.effectiveDistanceInches = effectiveDistanceInches;
            this.radialSpeedIps = radialSpeedIps;
            this.launcherOutput = launcherOutput;
            this.withinTurretRange = withinTurretRange;
            this.hasValidOwnAllianceCell = hasValidOwnAllianceCell;
            this.usedAprilTagTrim = usedAprilTagTrim;
            this.tipStateChangedThisCycle = tipStateChangedThisCycle;
        }
    }

    private final Alliance alliance;
    private final List<HiveCell> allCells;
    private final Vec2 turretOffsetFromRobotCenter;
    private final double fieldMidlineX;
    private final LaunchSpeedModel speedModel;
    private final double aprilTagTxSign;
    private final double aprilTagMaxSpeedIpsForTrim;
    private final double maxVisionTrimDeg;
    private final int tipConfirmDetections;
    private final int missesBeforeTip;

    private final Map<String, Integer> pendingTipCounts = new HashMap<>();

    public AdvancedTurretController(
            Alliance alliance,
            List<HiveCell> allCells,
            Vec2 turretOffsetFromRobotCenter,
            double fieldMidlineX,
            LaunchSpeedModel speedModel,
            double aprilTagTxSign,
            double aprilTagMaxSpeedIpsForTrim,
            double maxVisionTrimDeg,
            int tipConfirmDetections,
            int missesBeforeTip) {
        this.alliance = alliance;
        this.allCells = allCells;
        this.turretOffsetFromRobotCenter = turretOffsetFromRobotCenter;
        this.fieldMidlineX = fieldMidlineX;
        this.speedModel = speedModel;
        this.aprilTagTxSign = aprilTagTxSign;
        this.aprilTagMaxSpeedIpsForTrim = aprilTagMaxSpeedIpsForTrim;
        this.maxVisionTrimDeg = maxVisionTrimDeg;
        this.tipConfirmDetections = Math.max(1, tipConfirmDetections);
        this.missesBeforeTip = Math.max(1, missesBeforeTip);
    }

    /**
     * Toggles upwardFacing for the two cells sharing hiveId and resets debounce counters.
     */
    public void notifyHiveTipped(String hiveId) {
        for (HiveCell cell : allCells) {
            if (cell.alliance == alliance && cell.hiveId.equals(hiveId)) {
                cell.upwardFacing = !cell.upwardFacing;
                cell.consecutiveMisses = 0;
            }
        }
        pendingTipCounts.put(hiveId, 0);
    }

    /**
     * Computes turret angle and launcher output directly from Pedro Pathing's Follower,
     * Limelight's LLResult, and the current turret encoder angle (in degrees).
     */
    public AimSolution computeAim(Follower follower, LLResult llResult, double currentTurretDeg) {
        Pose pose = follower.pose();
        Vec2 robotPos = new Vec2(pose.x(), pose.y());
        double headingRad = pose.heading();
        Vec2 robotVel = new Vec2(follower.velocity().vx, follower.velocity().vy);

        boolean tipChanged = detectTipFromLimelight(llResult);

        Vec2 turretWorldPos = robotPos.add(turretOffsetFromRobotCenter.rotate(headingRad));

        HiveCell chosenCell = selectOwnAllianceUpwardCell(robotPos, headingRad, turretWorldPos, llResult);
        if (chosenCell == null) {
            return new AimSolution(
                    null,
                    Range.clip(currentTurretDeg, MIN_TURRET_DEG, MAX_TURRET_DEG),
                    currentTurretDeg,
                    0.0,
                    0.0,
                    0.0,
                    false,
                    false,
                    false,
                    tipChanged
            );
        }

        Vec2 targetPos = chosenCell.openingFieldPos;

        Vec2 baseDelta = targetPos.sub(turretWorldPos);
        double initialDist = Math.max(1.0, baseDelta.norm());
        double estOutput = speedModel.outputForDistanceInches(initialDist);
        double estBallSpeed = Math.max(1.0, speedModel.horizontalBallSpeedIps(estOutput));

        double tof = initialDist / estBallSpeed;
        Vec2 aimDelta = baseDelta;
        double effectiveDist = initialDist;
        double launcherOutput = estOutput;

        for (int i = 0; i < 5; i++) {
            aimDelta = baseDelta.sub(robotVel.scale(tof));
            effectiveDist = Math.max(1.0, aimDelta.norm());

            launcherOutput = speedModel.outputForDistanceInches(effectiveDist);
            double ballSpeed = Math.max(1.0, speedModel.horizontalBallSpeedIps(launcherOutput));
            tof = effectiveDist / ballSpeed;
        }

        Vec2 losUnit = baseDelta.scale(1.0 / initialDist);
        double radialSpeedIps = robotVel.dot(losUnit);

        double fieldAimAngleRad = Math.atan2(aimDelta.y, aimDelta.x);
        double robotRelativeRad = wrapAngleRad(fieldAimAngleRad - headingRad);
        double desiredTurretDeg = Math.toDegrees(robotRelativeRad);

        // Average tx across all visible AprilTags belonging to the chosen upward-facing cell
        boolean usedTagTrim = false;
        Double matchingClusterTx = getAverageTxForCell(llResult, chosenCell);
        if (chosenCell.upwardFacing
                && matchingClusterTx != null
                && robotVel.norm() <= aprilTagMaxSpeedIpsForTrim) {
            double clampedTx = Range.clip(aprilTagTxSign * matchingClusterTx, -maxVisionTrimDeg, maxVisionTrimDeg);
            double tagAngleRelativeToRobotDeg = currentTurretDeg + clampedTx;
            desiredTurretDeg = 0.65 * tagAngleRelativeToRobotDeg + 0.35 * desiredTurretDeg;
            usedTagTrim = true;
        }

        boolean inRange = desiredTurretDeg >= MIN_TURRET_DEG && desiredTurretDeg <= MAX_TURRET_DEG;
        double clampedTurretDeg = Range.clip(desiredTurretDeg, MIN_TURRET_DEG, MAX_TURRET_DEG);

        return new AimSolution(
                chosenCell,
                clampedTurretDeg,
                desiredTurretDeg,
                effectiveDist,
                radialSpeedIps,
                launcherOutput,
                inRange,
                true,
                usedTagTrim,
                tipChanged
        );
    }

    /**
     * Inspects all FiducialResults in LLResult to detect whether a hive has tipped.
     */
    private boolean detectTipFromLimelight(LLResult llResult) {
        if (llResult == null || !llResult.isValid()) {
            return false;
        }

        List<FiducialResult> fiducials = llResult.getFiducialResults();
        if (fiducials == null || fiducials.isEmpty()) {
            return false;
        }

        Set<String> contradictedHives = new HashSet<>();
        Set<String> confirmedHives = new HashSet<>();

        for (FiducialResult fiducial : fiducials) {
            int tagId = fiducial.getFiducialId();
            for (HiveCell cell : allCells) {
                if (cell.alliance != alliance) continue;

                boolean contradicts =
                        (cell.upwardFacing && cell.isTippedTag(tagId))
                                || (!cell.upwardFacing && cell.isUpwardTag(tagId));

                if (contradicts) {
                    contradictedHives.add(cell.hiveId);
                } else if (cell.matchesAnyTag(tagId)) {
                    confirmedHives.add(cell.hiveId);
                }
            }
        }

        boolean anyFlipped = false;
        for (String hiveId : contradictedHives) {
            int count = pendingTipCounts.getOrDefault(hiveId, 0) + 1;
            if (count >= tipConfirmDetections) {
                notifyHiveTipped(hiveId);
                anyFlipped = true;
            } else {
                pendingTipCounts.put(hiveId, count);
            }
        }

        for (String hiveId : confirmedHives) {
            if (!contradictedHives.contains(hiveId)) {
                pendingTipCounts.put(hiveId, 0);
            }
        }

        return anyFlipped;
    }

    /**
     * Returns the average horizontal offset (tx) across all detected fiducials that belong
     * to the cell's upwardTagIds cluster, or null if none are visible.
     */
    private Double getAverageTxForCell(LLResult llResult, HiveCell cell) {
        if (llResult == null || !llResult.isValid()) {
            return null;
        }

        List<FiducialResult> fiducials = llResult.getFiducialResults();
        if (fiducials == null || fiducials.isEmpty()) {
            return null;
        }

        double txSum = 0.0;
        int count = 0;
        for (FiducialResult fiducial : fiducials) {
            if (cell.isUpwardTag(fiducial.getFiducialId())) {
                txSum += fiducial.getTargetXDegrees();
                count++;
            }
        }

        return (count > 0) ? (txSum / count) : null;
    }

    private HiveCell selectOwnAllianceUpwardCell(
            Vec2 robotPos, double headingRad, Vec2 turretWorldPos, LLResult llResult) {
        boolean robotOnPositiveSide = robotPos.x >= fieldMidlineX;

        HiveCell bestInRangeUpward = null;
        double bestScore = Double.POSITIVE_INFINITY;

        HiveCell fallbackOwnUpward = null;
        double fallbackDist = Double.POSITIVE_INFINITY;

        for (HiveCell cell : allCells) {
            if (cell.alliance != alliance) continue;
            if (!cell.upwardFacing) continue;

            boolean cellOnPositiveSide = cell.openingFieldPos.x >= fieldMidlineX;
            if (cellOnPositiveSide != robotOnPositiveSide) continue;

            double dist = cell.openingFieldPos.sub(turretWorldPos).norm();
            if (dist < fallbackDist) {
                fallbackDist = dist;
                fallbackOwnUpward = cell;
            }

            double angleDeg = Math.toDegrees(
                    wrapAngleRad(
                            Math.atan2(
                                    cell.openingFieldPos.y - turretWorldPos.y,
                                    cell.openingFieldPos.x - turretWorldPos.x
                            ) - headingRad
                    )
            );

            if (angleDeg >= MIN_TURRET_DEG && angleDeg <= MAX_TURRET_DEG) {
                boolean seesUpwardTag = getAverageTxForCell(llResult, cell) != null;
                double score = Math.abs(angleDeg) + 0.1 * dist + (seesUpwardTag ? -25.0 : 0.0);
                if (score < bestScore) {
                    bestScore = score;
                    bestInRangeUpward = cell;
                }
            }
        }

        return (bestInRangeUpward != null) ? bestInRangeUpward : fallbackOwnUpward;
    }

    private static double wrapAngleRad(double angle) {
        while (angle > Math.PI) angle -= 2.0 * Math.PI;
        while (angle < -Math.PI) angle += 2.0 * Math.PI;
        return angle;
    }
}