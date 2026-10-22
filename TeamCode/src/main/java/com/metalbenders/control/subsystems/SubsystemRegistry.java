package com.metalbenders.control.subsystems;

import com.metalbenders.constants.Constants;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.HashMap;
import java.util.Map;

public class SubsystemRegistry {

    private final Map<Class<? extends CloseableSubsystem>, CloseableSubsystem> registry = new HashMap<>();
    public SubsystemRegistry(
            HardwareMap hardwareMap,
            Telemetry telemetry,
            Constants.AllianceColor allianceColor,
            Pose initialPose,
            Constants.OpModeType opModeType) {
        this.register(DriveSubsystem.class, new DriveSubsystem(hardwareMap, telemetry, initialPose));
        this.register(IntakeSubsystem.class, new IntakeSubsystem(hardwareMap, telemetry));
        this.register(LimelightSubsystem.class, new LimelightSubsystem(hardwareMap, telemetry, allianceColor));
        this.register(TurretSubsystem.class, new TurretSubsystem(hardwareMap, telemetry, allianceColor));
        if (opModeType == Constants.OpModeType.AUTO) {
            this.register(LidarSubsystem.class, new LidarSubsystem(hardwareMap, telemetry));
            this.register(HuskyLensSubsystem.class, new HuskyLensSubsystem(hardwareMap, telemetry, allianceColor));
        }
    }

    public <T extends CloseableSubsystem> void register(Class<T> clazz, T instance) {
        registry.put(clazz, instance);
    }

    public <T extends CloseableSubsystem> T get(Class<T> clazz) {
        return clazz.cast(registry.get(clazz));
    }

    public void closeAllSubsystems(Constants.OpModeType opModeType) {
        for (CloseableSubsystem subsystem : registry.values()) {
            subsystem.close(opModeType);
        }
    }
}