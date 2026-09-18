package dev.archdrift.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchitectureDriftPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "architectureDrift",
            ArchitectureDriftExtension::class.java,
        )

        extension.architectureFile.convention(
            project.layout.projectDirectory.file("architecture.drift"),
        )

        extension.sourceDirectory.convention(
            project.layout.projectDirectory.dir("src/main/kotlin"),
        )

        val architectureDriftCheck =
            project.tasks.register(
                "architectureDriftCheck",
                ArchitectureDriftCheckTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description =
                    "Checks the project for architecture drift."

                task.architectureFile.set(
                    extension.architectureFile,
                )

                task.sourceDirectory.set(
                    extension.sourceDirectory,
                )

                task.resultFile.convention(
                    project.layout.buildDirectory.file(
                        "architecture-drift/check-result.txt",
                    ),
                )

                task.baselineFile.set(
                    extension.baselineFile,
                )
            }

        project.tasks.matching { task ->
            task.name == "check"
        }.configureEach { task ->
            task.dependsOn(architectureDriftCheck)
        }
    }
}