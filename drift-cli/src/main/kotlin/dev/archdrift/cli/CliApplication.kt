package dev.archdrift.cli

import dev.archdrift.application.ArchitectureFileLoader
import dev.archdrift.application.ViolationBaselineCodec
import dev.archdrift.core.Architecture
import dev.archdrift.core.ViolationBaseline
import java.nio.file.Files
import java.nio.file.Path
import dev.archdrift.core.identity
import dev.archdrift.application.ArchitectureDriftFileAnalyzer
import dev.archdrift.application.ViolationBaselineFileLoader
import dev.archdrift.core.ForbiddenDependencyRule
import dev.archdrift.core.Violation

class CliApplication {

    fun run(args: Array<String>): CliResult =
        when (args.firstOrNull()) {
            "baseline" -> runBaseline(args)
            "check" -> runBaselineCheck(args)
            else -> runCheck(args)
        }

    private fun runCheck(
        args: Array<String>,
    ): CliResult {
        if (args.size < 2) {
            return CliResult(
                output = listOf(
                    "Usage: architecture-drift-detector <architecture-file> <kotlin-file-or-directory>",
                ),
                exitCode = 2,
            )
        }

        val architectureFile = Path.of(args[0])
        val sourcePath = Path.of(args[1])

        val architectureResult = loadArchitecture(architectureFile)
        if (architectureResult.error != null) {
            return architectureResult.error
        }

        val sourceError = validateSourcePath(
            sourcePath = sourcePath,
            originalArgument = args[1],
        )

        if (sourceError != null) {
            return sourceError
        }

        val violations = DriftFileAnalyzer(
            architecture = architectureResult.architecture!!,
        ).detect(sourcePath)

        if (violations.isEmpty()) {
            return CliResult(
                output = listOf(
                    "No architecture drift detected.",
                ),
                exitCode = 0,
            )
        }

        return CliResult(
            output = listOf(
                "Architecture drift detected:",
            ) + violations,
            exitCode = 1,
        )
    }

    private fun runBaseline(
        args: Array<String>,
    ): CliResult {
        if (args.size < 4) {
            return CliResult(
                output = listOf(
                    "Usage: architecture-drift-detector baseline " +
                            "<architecture-file> " +
                            "<kotlin-file-or-directory> " +
                            "<baseline-file>",
                ),
                exitCode = 2,
            )
        }

        val architectureFile = Path.of(args[1])
        val sourcePath = Path.of(args[2])
        val baselineFile = Path.of(args[3])

        val architectureResult = loadArchitecture(architectureFile)
        if (architectureResult.error != null) {
            return architectureResult.error
        }

        val sourceError = validateSourcePath(
            sourcePath = sourcePath,
            originalArgument = args[2],
        )

        if (sourceError != null) {
            return sourceError
        }

        val violations = ArchitectureDriftFileAnalyzer(
            architecture = architectureResult.architecture!!,
        ).detect(sourcePath)

        val baseline = ViolationBaseline(
            identities = violations
                .map { violation -> violation.identity() }
                .toSet(),
        )

        val content = ViolationBaselineCodec()
            .serialize(baseline)

        Files.writeString(
            baselineFile,
            content,
        )

        return CliResult(
            output = listOf(
                "Architecture drift baseline written to: $baselineFile",
            ),
            exitCode = 0,
        )
    }

    private fun loadArchitecture(
        architectureFile: Path,
    ): ArchitectureLoadResult {
        if (Files.notExists(architectureFile)) {
            return ArchitectureLoadResult(
                error = CliResult(
                    output = listOf(
                        "Error: Architecture file does not exist: $architectureFile",
                    ),
                    exitCode = 2,
                ),
            )
        }

        if (!Files.isRegularFile(architectureFile)) {
            return ArchitectureLoadResult(
                error = CliResult(
                    output = listOf(
                        "Error: Architecture path is not a file: $architectureFile",
                    ),
                    exitCode = 2,
                ),
            )
        }

        val architecture = try {
            ArchitectureFileLoader()
                .load(architectureFile)
        } catch (exception: IllegalArgumentException) {
            return ArchitectureLoadResult(
                error = CliResult(
                    output = listOf(
                        "Error: Invalid architecture definition: ${exception.message}",
                    ),
                    exitCode = 2,
                ),
            )
        }

        return ArchitectureLoadResult(
            architecture = architecture,
        )
    }

    private fun validateSourcePath(
        sourcePath: Path,
        originalArgument: String,
    ): CliResult? {
        if (Files.notExists(sourcePath)) {
            return CliResult(
                output = listOf(
                    "Error: Kotlin file does not exist: $originalArgument",
                ),
                exitCode = 2,
            )
        }

        if (
            !Files.isRegularFile(sourcePath) &&
            !Files.isDirectory(sourcePath)
        ) {
            return CliResult(
                output = listOf(
                    "Error: Input path must be a Kotlin file or directory: $sourcePath",
                ),
                exitCode = 2,
            )
        }

        if (
            Files.isRegularFile(sourcePath) &&
            !sourcePath.fileName.toString().endsWith(".kt")
        ) {
            return CliResult(
                output = listOf(
                    "Error: Input file must be a Kotlin source file: $sourcePath",
                ),
                exitCode = 2,
            )
        }

        return null
    }

    private fun runBaselineCheck(
        args: Array<String>,
    ): CliResult {
        if (args.size < 4) {
            return CliResult(
                output = listOf(
                    "Usage: architecture-drift-detector check " +
                            "<architecture-file> " +
                            "<kotlin-file-or-directory> " +
                            "<baseline-file>",
                ),
                exitCode = 2,
            )
        }

        val architectureFile = Path.of(args[1])
        val sourcePath = Path.of(args[2])
        val baselineFile = Path.of(args[3])

        val architectureResult = loadArchitecture(architectureFile)
        if (architectureResult.error != null) {
            return architectureResult.error
        }

        val sourceError = validateSourcePath(
            sourcePath = sourcePath,
            originalArgument = args[2],
        )

        if (sourceError != null) {
            return sourceError
        }

        if (Files.notExists(baselineFile)) {
            return CliResult(
                output = listOf(
                    "Error: Baseline file does not exist: $baselineFile",
                ),
                exitCode = 2,
            )
        }

        if (!Files.isRegularFile(baselineFile)) {
            return CliResult(
                output = listOf(
                    "Error: Baseline path is not a file: $baselineFile",
                ),
                exitCode = 2,
            )
        }

        val baseline = try {
            ViolationBaselineFileLoader().load(baselineFile)
        } catch (exception: IllegalArgumentException) {
            return CliResult(
                output = listOf(
                    "Error: Invalid architecture drift baseline: ${exception.message}",
                ),
                exitCode = 2,
            )
        }

        val violations = ArchitectureDriftFileAnalyzer(
            architecture = architectureResult.architecture!!,
        ).detect(sourcePath)

        val newViolations = baseline.newViolations(violations)

        if (newViolations.isEmpty()) {
            return CliResult(
                output = listOf(
                    "No new architecture drift detected.",
                ),
                exitCode = 0,
            )
        }

        return CliResult(
            output = listOf(
                "Architecture drift detected:",
            ) + newViolations.map(::formatViolation),
            exitCode = 1,
        )
    }

    private fun formatViolation(
        violation: Violation,
    ): String {
        val dependency = violation.dependency
        val location = dependency.location

        val rule = violation.rule as ForbiddenDependencyRule

        val prefix = if (location != null) {
            "${location.file}:${location.line}: "
        } else {
            ""
        }

        return prefix +
                "forbidden dependency ${rule.from.name} -> ${rule.to.name}: " +
                "${dependency.source} -> ${dependency.target}"
    }

    private data class ArchitectureLoadResult(
        val architecture: Architecture? = null,
        val error: CliResult? = null,
    )
}