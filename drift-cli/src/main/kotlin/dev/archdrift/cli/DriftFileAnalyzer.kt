package dev.archdrift.cli

import java.nio.file.Files
import java.nio.file.Path

class DriftFileAnalyzer {

    fun detect(sourceFile: Path): List<String> {
        val source = Files.readString(sourceFile)

        return DriftCli().detect(source)
    }
}