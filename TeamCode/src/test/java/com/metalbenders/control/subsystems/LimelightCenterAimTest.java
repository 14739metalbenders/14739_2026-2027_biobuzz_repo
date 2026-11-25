package com.metalbenders.control.subsystems;

import com.metalbenders.constants.AprilTag;
import com.metalbenders.constants.HiveAimOffsetConfig;
import com.metalbenders.util.HiveAimOffset;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.junit.Test;

import static org.junit.Assert.*;

public class LimelightCenterAimTest {
    @Test
    public void allTagGroupsProduceSameFaceRelativeOffsetAndCenterDistance() {
        double roll = 3 * Math.PI / 4;
        double[] xOffsets = {0.165, 0.070, -0.070, -0.165};
        double centerX = 0, centerY = Math.sqrt(0.5), centerZ = 3 * Math.sqrt(0.5);
        for (int id = 30; id <= 45; id++) {
            double yRotated = -0.190 * Math.cos(roll) + 0.150 * Math.sin(roll);
            double zRotated = -0.190 * Math.sin(roll) - 0.150 * Math.cos(roll);
            Pose3D tag = new Pose3D(new Position(DistanceUnit.METER,
                    centerX - xOffsets[(id - 30) % 4], centerY - yRotated,
                    centerZ - zRotated, 0),
                    new YawPitchRollAngles(AngleUnit.RADIANS, 0, 0, roll, 0));
            Pose3D center = LimelightSubsystem.computeCenterWithPoseOffset(id, tag);
            Position p = center.getPosition().toUnit(DistanceUnit.METER);
            assertEquals(centerX, p.x, 1e-8);
            assertEquals(centerY, p.y, 1e-8);
            assertEquals(centerZ, p.z, 1e-8);
            double centerDistance = Math.hypot(p.x, p.z * Math.sqrt(0.5) + p.y * Math.sqrt(0.5));
            HiveAimOffsetConfig.Table t = new HiveAimOffsetConfig.Table();
            t.offset1 = t.offset2 = t.offset3 = 2;
            HiveAimOffset.Result result = HiveAimOffset.calculate(center, t, -45, 0);
            assertNull("tag " + id + " " + AprilTag.fromId(id), result.error);
            assertEquals(0, result.viewingAngle, 1e-8);
            assertEquals(2, result.offsetInches, 1e-8);
            assertTrue(result.bearing > 0);
            assertEquals(2, centerDistance, 1e-8);
        }
    }
}
