package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("LFmotor");
        c.frontRightName.set("RFmotor");
        c.backLeftName.set("LRmotor");
        c.backRightName.set("RRmotor");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-2.5825641662117067);
        c.yPodOffset.set(-2.7749549685500736);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.3041784534423624);
                Controller secondaryTranslationalForward = Controller.proportional(0.11238581074898513);
                Controller primaryTranslationalLateral = Controller.proportional(0.4675480371634753);
                Controller secondaryTranslationalLateral = Controller.proportional(0.17274650661826216);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.01208714024857413));
                c.brake.set(Controller.proportionalFeedforward(0.01027406921128801));

                c.headingFeedback.set(Controller.proportional(3.797643898284665));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05923562082356446, 0.0033877768864465883));

                c.linearBrakeCoefficients.set(Matrix.diag(0.10072680440822356, 0.08314076966181598));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0010848410795951168, 0.0013308295093037194));

                c.maxAchievableForwardVelocity.set(79.03434291130888);
                c.maxAchievableStrafeVelocity.set(67.53896554271165);
                c.naturalForwardDeceleration.set(33.557764183581035);
                c.naturalStrafeDeceleration.set(65.08496771844237);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}