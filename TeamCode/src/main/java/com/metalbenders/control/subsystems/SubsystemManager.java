package com.metalbenders.control.subsystems;

import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.launcher.UpdateLauncher;
import com.metalbenders.util.AutonomousStorage;
import com.metalbenders.util.ShotReadinessIndicator;
import com.metalbenders.util.TurretController;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import static com.metalbenders.constants.Constants.OpModeType.AUTO;

public class SubsystemManager {
    private final Constants.OpModeType opModeType;
    private final Constants.AllianceColor allianceColor;
    private final ShotReadinessIndicator readinessIndicator = new ShotReadinessIndicator();
    private final Telemetry telemetry;
    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private LimelightSubsystem limelightSubsystem;
    private TurretSubsystem turretSubsystem;
    private LaunchSubsystem launchSubsystem;
    private FlowerToolSubsystem flowerToolSubsystem;
    private LightingSubsystem lightingSubsystem;
    private LidarSubsystem lidarSubsystem;
    private HuskyLensSubsystem huskyLensSubsystem;

    public SubsystemManager(
            HardwareMap hardwareMap,
            Telemetry telemetry,
            Constants.AllianceColor allianceColor,
            FieldSide startingActiveHiveCellFieldSide,
            Constants.OpModeType opModeType,
            Pose startingPose) {
        this.allianceColor = allianceColor;
        this.telemetry = telemetry;
        this.opModeType = opModeType;
        initialize(
                hardwareMap,
                telemetry,
                allianceColor,
                startingActiveHiveCellFieldSide,
                opModeType,
                startingPose);
    }

    private void initialize(
            HardwareMap hardwareMap,
            Telemetry telemetry,
            Constants.AllianceColor allianceColor,
            FieldSide startingActiveHiveCellFieldSide,
            Constants.OpModeType opModeType,
            Pose startingPose) {
        // Initialize subsystems
        intakeSubsystem = new IntakeSubsystem(hardwareMap, telemetry);
        limelightSubsystem = new LimelightSubsystem(
                hardwareMap, telemetry, allianceColor, startingActiveHiveCellFieldSide);
        turretSubsystem = new TurretSubsystem(hardwareMap, telemetry);
        driveSubsystem = new DriveSubsystem(hardwareMap, telemetry, startingPose,
                opModeType, limelightSubsystem, turretSubsystem);
        launchSubsystem = new LaunchSubsystem(hardwareMap, telemetry, opModeType);
        flowerToolSubsystem = new FlowerToolSubsystem(hardwareMap, telemetry);
        lightingSubsystem = new LightingSubsystem(hardwareMap, telemetry, allianceColor);

        //Autonomous specific subsystems
        if (opModeType == Constants.OpModeType.AUTO) {
            lidarSubsystem = new LidarSubsystem(hardwareMap, telemetry);
            huskyLensSubsystem = new HuskyLensSubsystem(hardwareMap, telemetry, allianceColor);
        }
        healthCheck();
    }

    public void healthCheck() {
        boolean errorDetected = !limelightSubsystem.isHealthy();
        if (!errorDetected && opModeType == AUTO) {
            if (lidarSubsystem != null && !lidarSubsystem.isHealthy()) {
                errorDetected = true;
            } else if (huskyLensSubsystem != null && !huskyLensSubsystem.isHealthy()) {
                errorDetected = true;
            }
        }
        if (errorDetected) {
            lightingSubsystem.setCurrentAnimation(Constants.IndicatorAnimationEnum.BLINK);
        } else {
            lightingSubsystem.setCurrentAnimation(Constants.IndicatorAnimationEnum.SOLID);
        }
    }

    public void updateReadinessIndicator() {
        boolean ready = readinessIndicator.update(
                launchSubsystem.getShotSequence(), launchSubsystem.isShooting(),
                limelightSubsystem.isHiveDetected(), launchSubsystem.getActualVelocity(),
                launchSubsystem.getTargetVelocity(), limelightSubsystem.getAimError(),
                TurretController.AIM_DEADBAND);
        lightingSubsystem.setCurrentColor(ready
                ? Constants.IndicatorLedEnum.GREEN
                : Constants.IndicatorLedEnum.valueOf(allianceColor.name()));
        telemetry.addData("Shot Ready", ready);
    }

    public void tearDown(boolean targetingInitialized) {
        if (opModeType == AUTO) {
            AutonomousStorage.save(targetingInitialized && driveSubsystem.hasFieldReferencedPose()
                            ? driveSubsystem.getCurrentPose() : null,
                    limelightSubsystem.getActiveHiveCellFieldSide(), allianceColor);
        }
        if (huskyLensSubsystem != null) {
            huskyLensSubsystem.teardown();
        }
        if (lidarSubsystem != null) {
            lidarSubsystem.teardown();
        }
        //teardown subsystems that are used in both autonomous and teleop
        intakeSubsystem.teardown();
        limelightSubsystem.teardown();
        turretSubsystem.teardown();
        launchSubsystem.teardown();
        driveSubsystem.teardown();
        flowerToolSubsystem.teardown();
    }

    public HuskyLensSubsystem getHuskyLensSubsystem() {
        return huskyLensSubsystem;
    }

    public LidarSubsystem getLidarSubsystem() {
        return lidarSubsystem;
    }

    public LightingSubsystem getLightingSubsystem() {
        return lightingSubsystem;
    }

    public FlowerToolSubsystem getFlowerToolSubsystem() {
        return flowerToolSubsystem;
    }

    public LaunchSubsystem getLaunchSubsystem() {
        return launchSubsystem;
    }

    public TurretSubsystem getTurretSubsystem() {
        return turretSubsystem;
    }

    public LimelightSubsystem getLimelightSubsystem() {
        return limelightSubsystem;
    }

    public IntakeSubsystem getIntakeSubsystem() {
        return intakeSubsystem;
    }

    public DriveSubsystem getDriveSubsystem() {
        return driveSubsystem;
    }
}
