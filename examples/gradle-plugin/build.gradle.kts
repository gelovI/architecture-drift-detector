plugins {
    base
    id("dev.archdrift")
}

architectureDrift {
    baselineFile.set(
        layout.projectDirectory.file("architecture-drift.baseline")
    )
}