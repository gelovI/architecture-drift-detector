package dev.archdrift.gradle

import dev.archdrift.application.ArchitectureDriftFileAnalyzer
import dev.archdrift.application.ArchitectureFileLoader
import dev.archdrift.core.ForbiddenDependencyRule
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import dev.archdrift.application.ViolationBaselineFileLoader
import org.gradle.api.tasks.Optional

abstract class ArchitectureDriftCheckTask : DefaultTask() {

    @get:InputFile
    abstract val architectureFile: RegularFileProperty

    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val baselineFile: RegularFileProperty

    @TaskAction
    fun checkArchitecture() {
        val architecturePath =
            architectureFile.get().asFile.toPath()

        val sourcePath =
            sourceDirectory.get().asFile.toPath()

        if (!Files.isRegularFile(architecturePath)) {
            throw GradleException(
                "Architecture drift configuration error: " +
                        "architecture file does not exist: $architecturePath",
            )
        }

        if (!Files.isDirectory(sourcePath)) {
            throw GradleException(
                "Architecture drift configuration error: " +
                        "source directory does not exist: $sourcePath",
            )
        }

        val architecture = ArchitectureFileLoader()
            .load(architecturePath)

        val violations = ArchitectureDriftFileAnalyzer(
            architecture = architecture,
        ).detect(sourcePath)

        val relevantViolations =
            if (baselineFile.isPresent) {
                val baseline = ViolationBaselineFileLoader()
                    .load(
                        baselineFile.get().asFile.toPath(),
                    )

                baseline.newViolations(violations)
            } else {
                violations
            }

        if (relevantViolations.isNotEmpty()) {
            val diagnostics = relevantViolations.joinToString("\n") { violation ->
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

        val resultPath = resultFile.get().asFile.toPath()

        Files.createDirectories(resultPath.parent)
        val successMessage =
            if (baselineFile.isPresent) {
                "No new architecture drift detected.\n"
            } else {
                "No architecture drift detected.\n"
            }

        Files.writeString(
            resultPath,
            successMessage,
        )
    }
}