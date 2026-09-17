package dev.archdrift.cli

import dev.archdrift.core.Architecture
import java.nio.file.Files
import java.nio.file.Path

class DriftFileAnalyzer(
    private val architecture: Architecture,
) {

    fun detect(sourceFile: Path): List<String> {
        val source = Files.readString(sourceFile)

        return DriftCli(
            architecture = architecture,
        ).detect(source)
    }
}