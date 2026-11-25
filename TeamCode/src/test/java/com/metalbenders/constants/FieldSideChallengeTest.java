package com.metalbenders.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldSideChallengeTest {
    @Test
    public void noCrossKeepsOnlyChallengesOnStartingSideForBothAlliances() {
        for (Constants.AllianceColor alliance : Constants.AllianceColor.values()) {
            for (FieldSide startingSide : FieldSide.values()) {
                for (Constants.AutoChallengeType challenge : Constants.AutoChallengeType.values()) {
                    if (challenge == Constants.AutoChallengeType.PRELOAD
                            || challenge == Constants.AutoChallengeType.PARK) {
                        assertTrue(FieldSide.shouldIncludeChallenge(challenge, alliance, startingSide, false));
                    } else {
                        assertEquals(FieldSide.getChallengeFieldSide(challenge, alliance) == startingSide,
                                FieldSide.shouldIncludeChallenge(challenge, alliance, startingSide, false));
                    }
                    assertTrue(FieldSide.shouldIncludeChallenge(challenge, alliance, startingSide, true));
                }
            }
        }
    }

    @Test
    public void flowerAndGardenSidesFollowAlliance() {
        assertEquals(FieldSide.AUDIENCE, FieldSide.getChallengeFieldSide(
                Constants.AutoChallengeType.GARDEN, Constants.AllianceColor.RED));
        assertEquals(FieldSide.SCORING, FieldSide.getChallengeFieldSide(
                Constants.AutoChallengeType.HIVE_SIDE_FLOWER, Constants.AllianceColor.RED));
        assertEquals(FieldSide.SCORING, FieldSide.getChallengeFieldSide(
                Constants.AutoChallengeType.PARK_SIDE_FLOWER, Constants.AllianceColor.BLUE));
        assertFalse(FieldSide.shouldIncludeChallenge(Constants.AutoChallengeType.HIVE_SIDE_FLOWER,
                Constants.AllianceColor.BLUE, FieldSide.SCORING, false));
    }
}
