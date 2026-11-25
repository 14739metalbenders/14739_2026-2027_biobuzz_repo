package com.metalbenders.util;

import com.metalbenders.constants.FieldSide;
import com.pedropathing.math.Pose;
import org.junit.Test;

import static com.metalbenders.constants.Constants.AllianceColor.BLUE;
import static com.metalbenders.constants.Constants.AllianceColor.RED;
import static org.junit.Assert.*;

public class AutonomousStorageTest {
    @Test
    public void autoHandoffIsSameAllianceAndOneUse() {
        AutonomousStorage.save(new Pose(10, 20, 0), FieldSide.AUDIENCE, RED);
        assertNull(AutonomousStorage.consume(BLUE));
        assertNull(AutonomousStorage.consume(RED));

        AutonomousStorage.save(new Pose(10, 20, 0), FieldSide.AUDIENCE, RED);
        AutonomousStorage.Handoff handoff = AutonomousStorage.consume(RED);
        assertNotNull(handoff);
        assertEquals(10, handoff.pose.x(), 0);
        assertSame(FieldSide.AUDIENCE, handoff.hiveSide);
        assertNull(AutonomousStorage.consume(RED));
    }
}
