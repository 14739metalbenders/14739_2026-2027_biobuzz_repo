package com.metalbenders.constants;

/**
 * Measurements for {@code LimelightPoseEstimator}.
 * Replace each robot measurement's Double.NaN with a number. Add measurements for
 * each tag you want to use. The estimator cannot use a tag until it is measured.
 */
public final class LimelightLocalizationConstants {
    private LimelightLocalizationConstants() {
    }

    // All distances below are in inches. Stand behind the robot and face forward.
    // Find the center of the robot's frame. Then find the center of the turret's
    // vertical rotation shaft. Measure horizontally between those two points:
    // X: forward is positive, backward is negative.
    // Y: left is positive, right is negative.
    public static double TURRET_AXIS_X = -2.08;
    public static double TURRET_AXIS_Y = 0.0;

    // Turn the turret until its encoder reads zero. From the turret shaft, measure
    // horizontally to the center of the camera lens, using the same X/Y directions
    // above. Measure forward/backward and left/right separately, not diagonally.
    public static double CAMERA_FROM_TURRET_X = 3.03;
    public static double CAMERA_FROM_TURRET_Y = 0.0;

    // With the turret still at zero, measure the camera angles in degrees:
    // Yaw: how far the lens points left of robot forward; right is negative.
    // Pitch: how far the lens points down from level; up is negative.
    // Roll: how far the camera is tilted sideways; left side up is positive.
    // Use 0 for an angle only if the camera is actually aligned that way.
    // For combined angles, measure roll first, then pitch, then yaw.
    // Camera height is not needed for this flat-field calculation.
    public static double CAMERA_YAW = 0.0;
    public static double CAMERA_PITCH = -45.0;
    public static double CAMERA_ROLL = 0.0;

    // Watch the encoder while turning the turret left: use 1 if its reading
    // increases, or -1 if its reading decreases.
    public static double TURRET_ANGLE_DIRECTION = 1;

    /**
     * Measurements for tags 30 through 45, on either alliance's hive.
     *
     * Use the official field CAD to find the straight shaft that the tag's hive
     * rotates around. Pick one point on that shaft and use that SAME point for
     * both the field and tag measurements below. All distances are in inches.
     *
     * pivotFieldX/Y: Find the chosen shaft point's X and Y in the same Pedro field
     * coordinates used by the drive system. Measure from Pedro's field origin.
     *
     * pivotTagX/Y/Z: Start at the center of the tag and measure to the chosen shaft
     * point, separately along the tag's X, Y, and Z directions. Looking at the tag,
     * X points right, Y points down, and Z points straight out from the tag's face.
     * Use negative numbers when the shaft point is in the opposite direction.
     *
     * axisTagX/Y/Z and axisFieldX/Y describe the shaft's direction, not a distance.
     * Pick a direction along the shaft and draw an arrow in the CAD. For each
     * coordinate system, measure how much a 1-inch arrow moves along each axis:
     * the tag's X/Y/Z for axisTag, and Pedro's field X/Y for axisField. For example,
     * an arrow pointing entirely in positive field X has axisFieldX = 1 and
     * axisFieldY = 0. Use the SAME arrow direction for both sets of numbers.
     * The shaft must be horizontal, so it has no field Z component.
     *
     * Tag coordinate reference:
     * https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-coordinate-systems
     *
     * To add a tag, copy the template below and replace each Double.NaN with its
     * measurement. Set the number inside TAGS[...] to the tag ID minus 30:
     * tag 30 uses TAGS[0], tag 31 uses TAGS[1], and tag 45 uses TAGS[15].
     */
    public static final TagGeometry[] TAGS = new TagGeometry[16];

    static {
        // Tag 30
        TAGS[0] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 31
        TAGS[1] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 32
        TAGS[2] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                -2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 33
        TAGS[3] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                -6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 34
        TAGS[4] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 35
        TAGS[5] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 36
        TAGS[6] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                -2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 37
        TAGS[7] = new TagGeometry(
                58.35, 70.47,              // pivotFieldX, pivotFieldY
                -6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 38
        TAGS[8] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 39
        TAGS[9] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 40
        TAGS[10] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                -2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 41
        TAGS[11] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                -6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                1, 0, 0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 42
        TAGS[12] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 43
        TAGS[13] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 44
        TAGS[14] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                -2.75, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
        // Tag 45
        TAGS[15] = new TagGeometry(
                83.86, 70.47,              // pivotFieldX, pivotFieldY
                -6.5, 14.267, -1.5625,  // pivotTagX, pivotTagY, pivotTagZ
                -1.0, 0.0, 0.0,  // axisTagX, axisTagY, axisTagZ
                1.0, 0.0);             // axisFieldX, axisFieldY
    }

    public static final class TagGeometry {
        public final double pivotFieldX, pivotFieldY;
        public final double pivotTagX, pivotTagY, pivotTagZ;
        public final double axisTagX, axisTagY, axisTagZ;
        public final double axisFieldX, axisFieldY;

        public TagGeometry(double pivotFieldX, double pivotFieldY,
                           double pivotTagX, double pivotTagY, double pivotTagZ,
                           double axisTagX, double axisTagY, double axisTagZ,
                           double axisFieldX, double axisFieldY) {
            this.pivotFieldX = pivotFieldX;
            this.pivotFieldY = pivotFieldY;
            this.pivotTagX = pivotTagX;
            this.pivotTagY = pivotTagY;
            this.pivotTagZ = pivotTagZ;
            this.axisTagX = axisTagX;
            this.axisTagY = axisTagY;
            this.axisTagZ = axisTagZ;
            this.axisFieldX = axisFieldX;
            this.axisFieldY = axisFieldY;
        }
    }
}
