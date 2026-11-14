# Autonomous strategy maps

`new_images` contains 136 nominal strategy maps: every ordered selection allowed by
the configuration menu, both waypoint series, and both starting states. Each image
contains separate red and blue alliance diagrams. Filenames list the challenge
types in execution order, followed by `INNER` or `OUTER` and `ACTIVE` or
`INACTIVE`. `NO_CHALLENGES` represents the empty selection.

Preload is first when selected and is required for collection challenges; park
is optional and last. Collection types are unique but may appear in any order.
The park command has a 25-second override, so it may interrupt the listed route.

The maps assume that an active-start preload immediately tips the hive, after
which every two shooting rounds tip it again. For an inactive start, the partner's
preload first tips the hive to the robot's starting side, and every two of our
shooting rounds then tip it. Each selected shooting challenge is one round.
These are planning assumptions, not a prediction of the live sensor state.
The robot starts on the audience side for red and scoring side for blue when
active, and on the opposite side when inactive.

Numbered stops and arrows show schematic straight-line connections between the
configured field locations, including the current-side launch check before a
cross-field shooting trip and the approximately 0.42-foot flower backup. Map
axes and approximate driving distances are shown in feet. They are not verified
collision-free trajectories. Actual paths and shooting sides can differ because
of live hive detection, motion timeouts, partner actions, or the park override;
in particular, the current autonomous command's transition checks are not all
enabled. Field structures are schematic and not to scale for collision planning.

Regenerate from the repository root with a Java 17 or newer JDK:

```powershell
java autonomous_field_maps\GenerateMaps.java
```

The generator reflects the current `FieldLocation`, `FieldSide`, autonomous
commands, and configuration rules; update it if those change.
