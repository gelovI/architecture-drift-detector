package dev.archdrift.cli

import java.nio.file.Files
import java.nio.file.Path

class CliApplication {

    fun run(args: Array<String>): CliResult {
        if (args.size < 2) {
            return CliResult(
                output = listOf(
                    "Usage: architecture-drift-detector <architecture-file> <kotlin-file>",
                ),
                exitCode = 2,
            )
        }

        val architectureFile = Path.of(args[0])
        val sourceFile = Path.of(args[1])

        if (Files.notExists(sourceFile)) {
            return CliResult(
                output = listOf(
                    "Error: Kotlin file does not exist: ${args[1]}",
                ),
                exitCode = 2,
            )
        }

        if (!Files.isRegularFile(sourceFile)) {
            return CliResult(
                output = listOf(
                    "Error: Input path is not a file: $sourceFile",
                ),
                exitCode = 2,
            )
        }

        if (!sourceFile.fileName.toString().endsWith(".kt")) {
            return CliResult(
                output = listOf(
                    "Error: Input file must be a Kotlin source file: $sourceFile",
                ),
                exitCode = 2,
            )
        }

        val architecture = ArchitectureFileLoader()
            .load(architectureFile)

        val violations = DriftFileAnalyzer(
            architecture = architecture,
        ).detect(sourceFile)

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
}