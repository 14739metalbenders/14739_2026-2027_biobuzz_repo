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
                Controller primaryTranslationalForward = Controller.proportional(0.25550146865249224);
                Controller secondaryTranslationalForward = Controller.proportional(0.09440096554211651);
                Controller primaryTranslationalLateral = Controller.proportional(0.3661210227219453);
                Controller secondaryTranslationalLateral = Controller.proportional(0.13527193496185677);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.01232488250043226));
                c.brake.set(Controller.proportionalFeedforward(0.010476150125367421));

                c.headingFeedback.set(Controller.proportional(3.1898910890438152));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04745988314953308, 0.0038354418810093494));

                c.linearBrakeCoefficients.set(Matrix.diag(0.10953438940016556, 0.060264849796201746));
                c.quadraticBrakeCoefficients.set(Matrix.diag(7.87494869456078E-4, 0.001548224323905596));

                c.maxAchievableForwardVelocity.set(80.95365738776422);
                c.maxAchievableStrafeVelocity.set(66.04576838587914);
                c.naturalForwardDeceleration.set(37.71136602501946);
                c.naturalStrafeDeceleration.set(74.3743835906193);
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