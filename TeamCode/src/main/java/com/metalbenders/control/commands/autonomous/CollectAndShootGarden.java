package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;
import com.metalbenders.control.subsystems.LaunchSubsystem;
import com.metalbenders.control.subsystems.LimelightSubsystem;
import com.metalbenders.control.subsystems.TurretSubsystem;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_GARDEN_INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL;
import static com.metalbenders.constants.FieldLocation.GARDEN_END;
import static com.metalbenders.constants.FieldLocation.GARDEN_START;

public class CollectAndShootGarden extends SequentialCommandGroup {

    public CollectAndShootGarden(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            LaunchSubsystem launchSubsystem,
            TurretSubsystem turretSubsystem,
            LimelightSubsystem limelightSubsystem,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            Supplier<FieldSide> activeHiveCellFieldSideSupplier) {
        super (
                new DriveToTargetFieldSide(
                        driveSubsystem,
                        () -> FieldSide.getGardenFieldSide(allianceColor),
                        allianceColor,
                        waypointSeriesType),
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor))
                        .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                new RunIntake(intakeSubsystem, INTAKE),
                new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                        .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_INTAKE),
                new StopIntake(intakeSubsystem),
                new DriveToLaunchAndShoot(
                        driveSubsystem,
                        intakeSubsystem,
                        launchSubsystem,
                        turretSubsystem,
                        limelightSubsystem,
                        allianceColor,
                        waypointSeriesType,
                        activeHiveCellFieldSideSupplier));
    }
}
