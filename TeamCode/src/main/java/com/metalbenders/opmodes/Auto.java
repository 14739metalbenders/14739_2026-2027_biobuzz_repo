package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.autonomous.AutoAbort;
import com.metalbenders.control.commands.autonomous.AutonomousSequence;
import com.metalbenders.control.commands.flowertool.SetFlowerToolState;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.autonomous.AutoPark;
import com.metalbenders.control.commands.autonomous.AutoShoot;
import com.metalbenders.control.commands.autonomous.CollectAndShootFlower;
import com.metalbenders.control.commands.autonomous.CollectAndShootGarden;
import com.metalbenders.control.commands.autonomous.ShootPreload;
import com.metalbenders.control.commands.intake.StopIntake;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.util.List;
import java.util.function.BooleanSupplier;

import static com.metalbenders.constants.Constants.AUTO_PARK_OVERRIDE_DELAY_MILLIS;
import static com.metalbenders.constants.Constants.AutoChallengeType.PARK;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;

@Autonomous(name = "Auto", group = "Auto", preselectTeleOp = "Teleop")
public class Auto extends Base {
    protected List<Constants.AutoChallengeType> autoStrategy;
    protected Constants.WaypointSeriesType waypointSeriesType;
    private SequentialCommandGroup autoCommands;
    private AutoAbort abort;
    protected boolean startingOnActiveSide;
    protected boolean allowFieldCrossing;
    private boolean strategyScheduled = false;

    @Override
    public void initialize() {
        super.initialize();
        abort = new AutoAbort();
        SequentialCommandGroup commands = new SequentialCommandGroup();
        for (Constants.AutoChallengeType autoChallengeType : autoStrategy) {
            if (!FieldSide.shouldIncludeChallenge(autoChallengeType, allianceColor,
                    FieldSide.getRobotStartingFieldSide(allianceColor, startingOnActiveSide),
                    allowFieldCrossing)) {
                continue;
            }
            switch (autoChallengeType) {
                case PRELOAD:
                    addPreload(commands);
                    break;
                case GARDEN:
                    addGarden(commands);
                    break;
                case PARK_SIDE_FLOWER:
                case HIVE_SIDE_FLOWER:
                    addFlower(commands, autoChallengeType);
                    break;
            }
        }
        if (autoStrategy.contains(PARK)) {
            autoCommands = addParkOverride(commands);
        } else {
            autoCommands = AutonomousSequence.withoutPark(commands, abort, createCleanupCommands());
        }
    }

    @Override
    public void run() {
        if (!strategyScheduled) {
            CommandScheduler.getInstance().schedule(autoCommands);
            strategyScheduled = true;
        }
        super.run();
    }

    private void addPreload(SequentialCommandGroup commands) {
        commands.addCommands(
                new ShootPreload(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        !startingOnActiveSide,
                        allowFieldCrossing,
                        abort));
    }

    private void addGarden(SequentialCommandGroup commands) {
        addCollectionCommand(commands, Constants.AutoChallengeType.GARDEN,
                new CollectAndShootGarden(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        allowFieldCrossing,
                        abort));
    }

    private void addFlower(SequentialCommandGroup commands, Constants.AutoChallengeType autoChallengeType) {
        addCollectionCommand(commands, autoChallengeType,
                new CollectAndShootFlower(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        autoChallengeType,
                        allowFieldCrossing,
                        abort));
    }

    private void addCollectionCommand(SequentialCommandGroup commands,
                                      Constants.AutoChallengeType type, Command collection) {
        if (allowFieldCrossing) {
            commands.addCommands(collection);
        } else {
            commands.addCommands(guardCollection(collection,
                    () -> FieldSide.getCurrentFieldSide(subsystemManager.getDriveSubsystem().getCurrentPose())
                            == FieldSide.getChallengeFieldSide(type, allianceColor)));
        }
    }

    static Command guardCollection(Command collection, BooleanSupplier sameSide) {
        return new ConditionalCommand(collection, new InstantCommand(), sameSide);
    }

    private SequentialCommandGroup addParkOverride(SequentialCommandGroup commands) {
        return AutonomousSequence.withPark(
                commands,
                new WaitCommand(AUTO_PARK_OVERRIDE_DELAY_MILLIS),
                abort,
                createCleanupCommands(),
                new AutoPark(subsystemManager, allianceColor, waypointSeriesType),
                new AutoShoot(subsystemManager, new InstantCommand(),
                        () -> subsystemManager.getLimelightSubsystem().isHiveDetected()),
                () -> subsystemManager.getLimelightSubsystem().isHiveDetected());
    }

    private ParallelCommandGroup createCleanupCommands() {
        return new ParallelCommandGroup(
                new InstantCommand(() -> subsystemManager.getDriveSubsystem().stop()),
                new StopIntake(subsystemManager),
                new SetLaunchGateState(subsystemManager, CLOSED),
                new SetFlowerToolState(subsystemManager, UP));
    }
}
