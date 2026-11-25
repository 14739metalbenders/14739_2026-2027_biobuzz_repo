package com.metalbenders.util;

import com.acmerobotics.dashboard.config.reflection.ReflectionConfig;
import com.acmerobotics.dashboard.config.variable.CustomVariable;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.constants.HiveAimOffsetConfig;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.junit.Test;

import static org.junit.Assert.*;

public class HiveAimOffsetTest {
    private static final double EPS = 1e-8;

    private static Pose3D pose(double x, double y, double z, double yaw, double pitch, double roll) {
        return new Pose3D(new Position(DistanceUnit.METER, x, y, z, 0),
                new YawPitchRollAngles(AngleUnit.RADIANS, yaw, pitch, roll, 0));
    }

    private static HiveAimOffsetConfig.Table table(double offset) {
        HiveAimOffsetConfig.Table table = new HiveAimOffsetConfig.Table();
        table.offset1 = table.offset2 = table.offset3 = offset;
        return table;
    }

    @Test
    public void selectsFourIndependentTables() {
        assertSame(HiveAimOffsetConfig.RED_SCORING, HiveAimOffsetConfig.forCell(Constants.AllianceColor.RED, FieldSide.SCORING));
        assertSame(HiveAimOffsetConfig.RED_AUDIENCE, HiveAimOffsetConfig.forCell(Constants.AllianceColor.RED, FieldSide.AUDIENCE));
        assertSame(HiveAimOffsetConfig.BLUE_SCORING, HiveAimOffsetConfig.forCell(Constants.AllianceColor.BLUE, FieldSide.SCORING));
        assertSame(HiveAimOffsetConfig.BLUE_AUDIENCE, HiveAimOffsetConfig.forCell(Constants.AllianceColor.BLUE, FieldSide.AUDIENCE));
        assertNotSame(HiveAimOffsetConfig.RED_SCORING, HiveAimOffsetConfig.BLUE_SCORING);
        assertNotSame(HiveAimOffsetConfig.RED_AUDIENCE, HiveAimOffsetConfig.BLUE_AUDIENCE);
        assertNull(HiveAimOffsetConfig.forCell(null, FieldSide.SCORING));
        assertNull(HiveAimOffsetConfig.forCell(Constants.AllianceColor.RED, null));
    }

    @Test
    public void tablesAreExposedAsDashboardFields() {
        CustomVariable config = ReflectionConfig.createVariableFromClass(HiveAimOffsetConfig.class);
        assertNotNull(config.getVariable("USE_OFF_CENTER_AIMING"));
        for (String name : new String[]{"RED_SCORING", "RED_AUDIENCE", "BLUE_SCORING", "BLUE_AUDIENCE"}) {
            CustomVariable table = (CustomVariable) config.getVariable(name);
            assertNotNull(name, table);
            for (String field : new String[]{"angle1", "offset1", "angle2", "offset2", "angle3", "offset3"}) {
                assertNotNull(name + "." + field, table.getVariable(field));
            }
        }
    }

    @Test
    public void interpolatesAndClampsAndRejectsBadTables() {
        HiveAimOffsetConfig.Table t = table(0);
        t.offset1 = -2; t.offset2 = 1; t.offset3 = 5;
        assertEquals(-2, HiveAimOffset.interpolate(t, -100), EPS);
        assertEquals(-0.5, HiveAimOffset.interpolate(t, -22.5), EPS);
        assertEquals(1, HiveAimOffset.interpolate(t, 0), EPS);
        assertEquals(3, HiveAimOffset.interpolate(t, 22.5), EPS);
        assertEquals(5, HiveAimOffset.interpolate(t, 100), EPS);
        t.angle2 = t.angle1;
        assertTrue(Double.isNaN(HiveAimOffset.interpolate(t, 0)));
        t.angle2 = 0;
        t.offset3 = Double.NaN;
        assertTrue(Double.isNaN(HiveAimOffset.interpolate(t, 0)));
        assertTrue(Double.isNaN(HiveAimOffset.interpolate(null, 0)));
    }

    @Test
    public void headOnAndCornersUseFaceRelativeSigns() {
        HiveAimOffsetConfig.Table t = table(2);
        Pose3D headOn = pose(0, 0, 2, 0, 0, Math.PI);
        HiveAimOffset.Result head = HiveAimOffset.calculate(headOn, t, 0, 0);
        assertNull(head.error);
        assertEquals(0, head.viewingAngle, EPS);
        assertEquals(2, head.offsetInches, EPS);
        assertTrue("left offset should produce a positive existing bearing", head.bearing > 0);
        HiveAimOffset.Result right = HiveAimOffset.calculate(pose(-1, 0, 2, 0, 0, Math.PI), t, 0, 0);
        HiveAimOffset.Result left = HiveAimOffset.calculate(pose(1, 0, 2, 0, 0, Math.PI), t, 0, 0);
        assertEquals(Math.toDegrees(Math.atan2(1, 2)), right.viewingAngle, EPS);
        assertEquals(-right.viewingAngle, left.viewingAngle, EPS);
        assertEquals(-Math.toDegrees(Math.atan2(1, 2 * Math.cos(Math.PI / 4))),
                HiveAimOffset.calculate(pose(1, 0, 2, 0, 0, Math.PI), table(0), 0, 0).bearing, EPS);
    }

    @Test
    public void pitchedCameraProducesSameViewAngleAndOffset() {
        HiveAimOffset.Result level = HiveAimOffset.calculate(pose(-1, -1, 2, 0, 0, Math.PI), table(2), 0, 0);
        double root = Math.sqrt(0.5);
        HiveAimOffset.Result pitched = HiveAimOffset.calculate(
                pose(-1, root, 3 * root, 0, 0, 3 * Math.PI / 4), table(2), -45, 0);
        assertNull(level.error);
        assertNull(pitched.error);
        assertEquals(level.viewingAngle, pitched.viewingAngle, EPS);
        assertEquals(level.offsetInches, pitched.offsetInches, EPS);
    }

    private static double[] toCamera(double[] v, double yaw, double pitch, double roll) {
        double x = Math.cos(yaw) * v[0] + Math.sin(yaw) * v[1];
        double y = -Math.sin(yaw) * v[0] + Math.cos(yaw) * v[1];
        double z = v[2];
        double x2 = Math.cos(pitch) * x - Math.sin(pitch) * z;
        double z2 = Math.sin(pitch) * x + Math.cos(pitch) * z;
        double y2 = Math.cos(roll) * y + Math.sin(roll) * z2;
        double z3 = -Math.sin(roll) * y + Math.cos(roll) * z2;
        return new double[]{-y2, -z3, x2};
    }

    private static Pose3D observedFrom(double yaw, double pitch, double roll) {
        double[] p = toCamera(new double[]{2, -1, 1}, yaw, pitch, roll);
        double[] x = toCamera(new double[]{0, -1, 0}, yaw, pitch, roll);
        double[] y = toCamera(new double[]{0, 0, 1}, yaw, pitch, roll);
        double[] z = toCamera(new double[]{-1, 0, 0}, yaw, pitch, roll);
        return pose(p[0], p[1], p[2], Math.atan2(x[1], x[0]), Math.asin(-x[2]),
                Math.atan2(y[2], z[2]));
    }

    @Test
    public void changingTurretYawAndCameraRollDoesNotChangeViewAngle() {
        double pitch = Math.toRadians(-35), roll = Math.toRadians(12);
        HiveAimOffset.Result first = HiveAimOffset.calculate(observedFrom(-0.2, pitch, roll),
                table(3), -35, 12);
        HiveAimOffset.Result second = HiveAimOffset.calculate(observedFrom(0.35, pitch, roll),
                table(3), -35, 12);
        assertNull(first.error);
        assertNull(second.error);
        assertEquals(-Math.toDegrees(Math.atan2(1, 2)), first.viewingAngle, EPS);
        assertEquals(first.viewingAngle, second.viewingAngle, EPS);
        assertEquals(first.offsetInches, second.offsetInches, EPS);
    }

    @Test
    public void aggregationNeverMultipliesOffsetOrMixesFallbackWithShiftedAim() {
        HiveAimOffset.Result shifted = HiveAimOffset.calculate(pose(0, 0, 2, 0, 0, Math.PI),
                table(2), 0, 0);
        HiveAimOffset.Aggregate aggregate = new HiveAimOffset.Aggregate();
        aggregate.add(0, shifted);
        aggregate.add(0, shifted);
        assertEquals(2, aggregate.result().offsetInches, EPS);
        assertEquals(shifted.bearing, aggregate.result().bearing, EPS);
        aggregate.add(6, HiveAimOffset.calculate(null, table(2), 0, 0));
        assertEquals(2, aggregate.result().bearing, EPS);
        assertNotNull(aggregate.result().error);
        assertTrue(Double.isNaN(aggregate.result().offsetInches));
        assertNotNull(new HiveAimOffset.Aggregate().result().error);
    }

    @Test
    public void disabledAimUsesCenterEvenWithOffsetsOrInvalidGeometry() {
        HiveAimOffset.Aggregate aggregate = new HiveAimOffset.Aggregate();
        HiveAimOffset.Result shifted = HiveAimOffset.calculate(pose(0, 0, 2, 0, 0, Math.PI),
                table(2), 0, 0);
        aggregate.add(0, shifted);
        assertTrue(aggregate.result(true).bearing > 0);
        assertEquals(0, aggregate.result(false).bearing, EPS);
        aggregate.add(4, null);
        assertEquals(2, aggregate.result(false).bearing, EPS);
        assertNull(aggregate.result(false).error);
        assertNotNull(aggregate.result(true).error);
        HiveAimOffset.Aggregate disabled = new HiveAimOffset.Aggregate();
        disabled.addCenter(-3);
        assertEquals(-3, disabled.result(false).bearing, EPS);
        assertNotNull(disabled.result(true).error);
    }

    @Test
    public void rejectsUnavailableBehindOrDegenerateGeometry() {
        assertNotNull(HiveAimOffset.calculate(null, table(0), -45, 0).error);
        assertNotNull(HiveAimOffset.calculate(pose(0, 0, 2, 0, 0, 0), table(0), 0, 0).error);
        assertNotNull(HiveAimOffset.calculate(pose(0, 0, 2, 0, 0, Math.PI / 2), table(0), 0, 0).error);
        assertNotNull(HiveAimOffset.calculate(pose(Double.NaN, 0, 2, 0, 0, Math.PI), table(0), 0, 0).error);
        assertEquals("Invalid offset table",
                HiveAimOffset.calculate(pose(0, 0, 2, 0, 0, Math.PI), null, 0, 0).error);
    }
}
