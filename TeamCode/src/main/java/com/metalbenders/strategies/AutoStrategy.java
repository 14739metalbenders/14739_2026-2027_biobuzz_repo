package com.metalbenders.strategies;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.autonomous.*;
import com.metalbenders.control.subsystems.*;

import static com.metalbenders.constants.Constants.AUTO_PARK_OVERRIDE_DELAY_MILLIS;
import static com.metalbenders.constants.Constants.HIVE_SWITCH_TIMEOUT_MILLIS;
import static com.metalbenders.constants.FieldLocation.OTHER_FLOWER;
import static com.metalbenders.constants.FieldLocation.PARK_SIDE_FLOWER;

public abstract class AutoStrategy {

    protected boolean startingOnActiveSide;
    protected Constants.AllianceColor allianceColor;
    protected DriveSubsystem driveSubsystem;
    protected IntakeSubsystem intakeSubsystem;
    protected LaunchSubsystem launchSubsystem;
    protected TurretSubsystem turretSubsystem;
    protected HuskyLensSubsystem huskyLensSubsystem;
    protected LimelightSubsystem limelightSubsystem;
    protected LidarSubsystem lidarSubsystem;
    protected FlowerToolSubsystem flowerToolSubsystem;
    private final boolean preload;
    private final boolean parkSideFlower;
    private final boolean otherFlower;
    private final boolean garden;
    private final boolean useInsideWaypoints;

    public AutoStrategy(
            boolean preload,
            boolean garden,
            boolean parkSideFlower,
            boolean otherFlower,
            boolean useInsideWaypoints) {
        this.preload = preload;
        this.parkSideFlower = parkSideFlower;
        this.otherFlower = otherFlower;
        this.garden = garden;
        this.useInsideWaypoints = useInsideWaypoints;
    }

    public void initialize(
            Constants.AllianceColor allianceColor,
            boolean startingOnActiveSide,
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            HuskyLensSubsystem huskyLensSubsystem,
            LimelightSubsystem limelightSubsystem,
            LidarSubsystem lidarSubsystem,
            FlowerToolSubsystem flowerToolSubsystem) {
        this.startingOnActiveSide = startingOnActiveSide;
        this.allianceColor = allianceColor;
        this.driveSubsystem = driveSubsystem;
        this.intakeSubsystem = intakeSubsystem;
        this.launchSubsystem = launchSubsystem;
        this.turretSubsystem = turretSubsystem;
        this.huskyLensSubsystem = huskyLensSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.lidarSubsystem = lidarSubsystem;
        this.flowerToolSubsystem = flowerToolSubsystem;
        CommandScheduler.getInstance().schedule(
                new SequentialCommandGroup(
                        new ParallelRaceGroup(
                                getCommands(),
                                new WaitCommand(AUTO_PARK_OVERRIDE_DELAY_MILLIS)),
                        new InstantCommand(() -> {
                            intakeSubsystem.setIntakeState(Constants.IntakeState.STOP);
                            launchSubsystem.setLaunchGateState(Constants.LaunchGateState.CLOSED);
                            flowerToolSubsystem.setFlowerToolState(Constants.FlowerToolState.UP);
                        }, intakeSubsystem, launchSubsystem, flowerToolSubsystem),
                        new AutoPark(
                                driveSubsystem,
                                this::getCurrentFieldSide,
                                allianceColor,
                                useInsideWaypoints)));
    }

    protected FieldSide getCurrentFieldSide() {
        return FieldSide.getCurrentFieldSide(driveSubsystem.getCurrentPose());
    }

    protected FieldSide getActiveHiveCellFieldSide() {
        return limelightSubsystem.getActiveHiveCellFieldSide();
    }

    private CommandBase getCommands() {
        SequentialCommandGroup commands = new SequentialCommandGroup();
        //PRELOAD
        if (preload) {
            commands.addCommands(
                    new AutoShoot(
                            driveSubsystem,
                            intakeSubsystem,
                            launchSubsystem,
                            startingOnActiveSide ? null : HIVE_SWITCH_TIMEOUT_MILLIS,
                            this::getCurrentFieldSide,
                            this::getActiveHiveCellFieldSide,
                            allianceColor,
                            useInsideWaypoints));
        }

        //GARDEN
        if (garden) {
            commands.addCommands(
                    new CollectGardenPollen(
                            driveSubsystem,
                            intakeSubsystem,
                            this::getCurrentFieldSide,
                            allianceColor,
                            useInsideWaypoints),
                    new AutoShoot(
                            driveSubsystem,
                            intakeSubsystem,
                            launchSubsystem,
                            this::getCurrentFieldSide,
                            this::getActiveHiveCellFieldSide,
                            allianceColor,
                            useInsideWaypoints));
        }

        //OTHER FLOWER
        if (otherFlower) {
            commands.addCommands(
                    new CollectFlowerPollen(
                            flowerToolSubsystem,
                            driveSubsystem,
                            intakeSubsystem,
                            OTHER_FLOWER,
                            this::getCurrentFieldSide,
                            allianceColor,
                            useInsideWaypoints),
                    new AutoShoot(
                            driveSubsystem,
                            intakeSubsystem,
                            launchSubsystem,
                            this::getCurrentFieldSide,
                            this::getActiveHiveCellFieldSide,
                            allianceColor,
                            useInsideWaypoints));
        }

        //PARK SIDE FLOWER
        if (parkSideFlower) {
            commands.addCommands(
                    new CollectFlowerPollen(
                            flowerToolSubsystem,
                            driveSubsystem,
                            intakeSubsystem,
                            PARK_SIDE_FLOWER,
                            this::getCurrentFieldSide,
                            allianceColor,
                            useInsideWaypoints),
                    new AutoShoot(
                            driveSubsystem,
                            intakeSubsystem,
                            launchSubsystem,
                            this::getCurrentFieldSide,
                            this::getActiveHiveCellFieldSide,
                            allianceColor,
                            useInsideWaypoints));
        }

        return commands;
    }
}
