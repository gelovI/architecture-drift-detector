package dev.archdrift.cli

import java.nio.file.Path

class CliApplication {

    fun run(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return listOf(
                "Usage: architecture-drift-detector <kotlin-file>",
            )
        }

        val violations = DriftFileAnalyzer()
            .detect(Path.of(args[0]))

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