package com.metalbenders.control.commands.autonomous;

import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.metalbenders.constants.Constants;
import com.metalbenders.constants.FieldSide;
import com.metalbenders.control.commands.DriveToPosition;
import com.metalbenders.control.commands.SetIntakeState;
import com.metalbenders.control.subsystems.DriveSubsystem;
import com.metalbenders.control.subsystems.IntakeSubsystem;

import java.util.function.Supplier;

import static com.metalbenders.constants.Constants.GARDEN_INTAKE_TIMEOUT_MILLIS;
import static com.metalbenders.constants.FieldLocation.GARDEN_END;
import static com.metalbenders.constants.FieldLocation.GARDEN_START;

public class CollectGardenPollen extends SequentialCommandGroup {

    public CollectGardenPollen(
            DriveSubsystem driveSubsystem,
            IntakeSubsystem intakeSubsystem,
            Supplier<FieldSide> currentFieldSide,
            Constants.AllianceColor allianceColor,
            boolean useInsideWaypoints) {
        SelectCommand driveToGardenSide = new SelectCommand(
                () -> new DriveToFieldSide(
                        driveSubsystem,
                        currentFieldSide.get(),
                        FieldSide.getGardenFieldSide(allianceColor),
                        allianceColor,
                        useInsideWaypoints));
        // Supplier-based selection does not inherit the selected command's requirements.
        driveToGardenSide.addRequirements(driveSubsystem);
        addCommands(
                driveToGardenSide,
                new DriveToPosition(driveSubsystem, GARDEN_START.getPose(allianceColor)),
                new ParallelDeadlineGroup(
                        new DriveToPosition(driveSubsystem, GARDEN_END.getPose(allianceColor))
                                .withTimeout(GARDEN_INTAKE_TIMEOUT_MILLIS),
                        new SetIntakeState(intakeSubsystem, Constants.IntakeState.INTAKE)),
                new SetIntakeState(intakeSubsystem, Constants.IntakeState.STOP));
    }
}
