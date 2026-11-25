package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.intake.RunIntake;
import com.metalbenders.control.commands.intake.StopIntake;
import com.metalbenders.control.subsystems.SubsystemManager;

import static com.metalbenders.constants.Constants.IntakeState.INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_GARDEN_INTAKE;
import static com.metalbenders.constants.Constants.MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL;
import static com.metalbenders.constants.FieldLocation.GARDEN_END;
import static com.metalbenders.constants.FieldLocation.GARDEN_START;

public class CollectAndShootGarden extends SequentialCommandGroup {

    public CollectAndShootGarden(
            SubsystemManager subsystemManager,
            Constants.AllianceColor allianceColor,
            Constants.WaypointSeriesType waypointSeriesType,
            boolean allowFieldCrossing,
            AutoAbort abort) {
        super (
                new DriveToTargetFieldSide(
                        subsystemManager,
                        () -> FieldSide.getChallengeFieldSide(Constants.AutoChallengeType.GARDEN, allianceColor),
                        allianceColor,
                        waypointSeriesType),
                new DriveToPosition(subsystemManager, GARDEN_START.getPose(allianceColor))
                        .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_TRAVEL),
                new RunIntake(subsystemManager, INTAKE),
                new DriveToPosition(subsystemManager, GARDEN_END.getPose(allianceColor))
                        .withTimeout(MILLIS_TO_WAIT_FOR_GARDEN_INTAKE),
                new StopIntake(subsystemManager),
                new DriveToLaunchAndShoot(
                        subsystemManager,
                        allianceColor,
                        waypointSeriesType,
                        allowFieldCrossing,
                        abort));
    }
}
