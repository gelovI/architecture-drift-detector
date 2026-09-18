package dev.archdrift.gradle

import dev.archdrift.application.ArchitectureDriftFileAnalyzer
import dev.archdrift.application.ArchitectureFileLoader
import dev.archdrift.core.ForbiddenDependencyRule
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Files

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
            project.tasks.register("architectureDriftCheck") { task ->
                task.group = "verification"
                task.description = "Checks the project for architecture drift."

                task.doLast {
                    val architectureFile =
                        extension.architectureFile
                            .get()
                            .asFile
                            .toPath()

                    val sourceDirectory =
                        extension.sourceDirectory
                            .get()
                            .asFile
                            .toPath()

                    if (!Files.isRegularFile(architectureFile)) {
                        throw GradleException(
                            "Architecture drift configuration error: " +
                                    "architecture file does not exist: $architectureFile",
                        )
                    }

                    if (!Files.isDirectory(sourceDirectory)) {
                        throw GradleException(
                            "Architecture drift configuration error: " +
                                    "source directory does not exist: $sourceDirectory",
                        )
                    }

                    val architecture = ArchitectureFileLoader()
                        .load(architectureFile)

                    val violations = ArchitectureDriftFileAnalyzer(
                        architecture = architecture,
                    ).detect(sourceDirectory)

                    if (violations.isNotEmpty()) {
                        val diagnostics = violations.joinToString("\n") { violation ->
                            val dependency = violation.dependency
                            val location = dependency.location

                            val ruleDescription = when (val rule = violation.rule) {
                                is ForbiddenDependencyRule ->
                                    "forbidden dependency ${rule.from.name} -> ${rule.to.name}"

                                else ->
                                    "architecture rule violation"
                            }

                            val dependencyDescription =
                                "${dependency.source} -> ${dependency.target}"

                            if (location == null) {
                                "$ruleDescription: $dependencyDescription"
                            } else {
                                "${location.file}:${location.line}: " +
                                        "$ruleDescription: $dependencyDescription"
                            }
                        }

                        throw GradleException(
                            "Architecture drift detected:\n$diagnostics",
                        )
                    }
                }
            }

        project.tasks.matching { task ->
            task.name == "check"
        }.configureEach { task ->
            task.dependsOn(architectureDriftCheck)
        }
    }
}