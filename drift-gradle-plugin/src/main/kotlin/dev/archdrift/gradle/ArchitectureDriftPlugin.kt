package dev.archdrift.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchitectureDriftPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("architectureDriftCheck") { task ->
            task.group = "verification"
            task.description = "Checks the project for architecture drift."
        }
    }
}