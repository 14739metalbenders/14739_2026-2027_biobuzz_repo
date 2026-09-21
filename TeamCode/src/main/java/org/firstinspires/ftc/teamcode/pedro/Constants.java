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
        c.xPodOffset.set(-3.257981247789278);
        c.yPodOffset.set(0.22570559358972264);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.4059964281897683);
        Controller secondaryTranslationalForward = Controller.proportional(0.15000483179175986);
        Controller primaryTranslationalLateral = Controller.proportional(0.6963075843016999);
        Controller secondaryTranslationalLateral = Controller.proportional(0.2572670467181598);

        c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
        c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

        c.coast.set(Controller.proportionalFeedforward(0.013020559987392915));
        c.brake.set(Controller.proportionalFeedforward(0.011067475989283978));

        c.headingFeedback.set(Controller.proportional(7.10762392307813));
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05873183681974509, 0.007387108348974771));

        c.linearBrakeCoefficients.set(Matrix.diag(0.10469430589406754, 0.07369931816930064));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0016273647277240575, 0.0017624556695648686));

        c.maxAchievableForwardVelocity.set(71.42762272862817);
        c.maxAchievableStrafeVelocity.set(55.93278507118735);
        c.naturalForwardDeceleration.set(32.00836195890927);
        c.naturalStrafeDeceleration.set(68.7306365741579);
    });

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}