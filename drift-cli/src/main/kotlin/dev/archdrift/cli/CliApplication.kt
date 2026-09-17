package dev.archdrift.cli

import java.nio.file.Files
import java.nio.file.Path

class CliApplication {

    fun run(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return listOf(
                "Usage: architecture-drift-detector <kotlin-file>",
            )
        }

        val sourceFile = Path.of(args[0])

        if (Files.notExists(sourceFile)) {
            return listOf(
                "Error: Kotlin file does not exist: ${args[0]}",
            )
        }

        if (!Files.isRegularFile(sourceFile)) {
            return listOf(
                "Error: Input path is not a file: $sourceFile",
            )
        }

        if (!sourceFile.fileName.toString().endsWith(".kt")) {
            return listOf(
                "Error: Input file must be a Kotlin source file: $sourceFile",
            )
        }

        val violations = DriftFileAnalyzer()
            .detect(sourceFile)

        if (violations.isEmpty()) {
            return listOf(
                "No architecture drift detected.",
            )
        }

        return listOf(
            "Architecture drift detected:",
        ) + violations
    }
}