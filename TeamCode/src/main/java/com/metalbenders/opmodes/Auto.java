package com.metalbenders.opmodes;

import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.metalbenders.constants.Constants;
import com.metalbenders.control.commands.flowertool.SetFlowerToolState;
import com.metalbenders.control.commands.launcher.SetLaunchGateState;
import com.metalbenders.control.commands.autonomous.AutoPark;
import com.metalbenders.control.commands.autonomous.CollectAndShootFlower;
import com.metalbenders.control.commands.autonomous.CollectAndShootGarden;
import com.metalbenders.control.commands.autonomous.ShootPreload;
import com.metalbenders.control.commands.intake.StopIntake;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.util.List;

import static com.metalbenders.constants.Constants.AUTO_PARK_OVERRIDE_DELAY_MILLIS;
import static com.metalbenders.constants.Constants.AutoChallengeType.PARK;
import static com.metalbenders.constants.Constants.FlowerToolState.UP;
import static com.metalbenders.constants.Constants.LaunchGateState.CLOSED;

@Autonomous(name = "Auto", group = "Auto", preselectTeleOp = "Teleop")
public class Auto extends Base {

    protected List<Constants.AutoChallengeType> autoStrategy;
    protected Constants.WaypointSeriesType waypointSeriesType;
    private SequentialCommandGroup autoCommands;
    protected boolean startingOnActiveSide;
    private boolean strategyScheduled = false;

    @Override
    public void initialize() {
        super.initialize();
        SequentialCommandGroup commands = new SequentialCommandGroup();
        for (Constants.AutoChallengeType autoChallengeType : autoStrategy) {
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
            autoCommands = commands;
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
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        limelightSubsystem::getActiveHiveCellFieldSide,
                        !startingOnActiveSide));
    }

    private void addGarden(SequentialCommandGroup commands) {
        commands.addCommands(
                new CollectAndShootGarden(
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        limelightSubsystem::getActiveHiveCellFieldSide));
    }

    private void addFlower(SequentialCommandGroup commands, Constants.AutoChallengeType autoChallengeType) {
        commands.addCommands(
                new CollectAndShootFlower(
                        flowerToolSubsystem,
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        autoChallengeType,
                        limelightSubsystem::getActiveHiveCellFieldSide));
    }

    private SequentialCommandGroup addParkOverride(SequentialCommandGroup commands) {
        return new SequentialCommandGroup(
                new ParallelRaceGroup(
                        commands,
                        new WaitCommand(AUTO_PARK_OVERRIDE_DELAY_MILLIS)
                ),
                new ParallelCommandGroup(
                        new InstantCommand(() -> driveSubsystem.stop()),
                        new StopIntake(intakeSubsystem),
                        new SetLaunchGateState(launchSubsystem, CLOSED),
                        new SetFlowerToolState(flowerToolSubsystem, UP)
                ),
                new AutoPark(driveSubsystem, allianceColor, waypointSeriesType));
    }
}
