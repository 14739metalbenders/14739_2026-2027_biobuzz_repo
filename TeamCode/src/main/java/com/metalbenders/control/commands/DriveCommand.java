package com.metalbenders.control.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.metalbenders.control.subsystems.DriveSubsystem;

import java.util.function.DoubleSupplier;

public class DriveCommand extends CommandBase {

    private final DriveSubsystem driveSubsystem;
    private final DoubleSupplier forwardSupplier;
    private final DoubleSupplier strafeSupplier;
    private final DoubleSupplier headingSupplier;

    public DriveCommand(DriveSubsystem driveSubsystem,
                        DoubleSupplier forwardSupplier,
                        DoubleSupplier strafeSupplier,
                        DoubleSupplier headingSupplier) {
        this.driveSubsystem = driveSubsystem;
        this.forwardSupplier = forwardSupplier;
        this.strafeSupplier = strafeSupplier;
        this.headingSupplier = headingSupplier;
        addRequirements(driveSubsystem);
    }

    @Override
    public void execute() {
        driveSubsystem.drive(
                forwardSupplier.getAsDouble(),
                strafeSupplier.getAsDouble(),
                headingSupplier.getAsDouble(),
                true // true = Field Centric; false = Robot Centric
        );
    }
}