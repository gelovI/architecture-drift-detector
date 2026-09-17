package dev.archdrift.cli

import dev.archdrift.core.Architecture
import java.nio.file.Files
import java.nio.file.Path

class DriftFileAnalyzer(
    private val architecture: Architecture,
) {

    fun detect(sourcePath: Path): List<String> {
        val sourceFiles = if (Files.isDirectory(sourcePath)) {
            findKotlinFiles(sourcePath)
        } else {
            listOf(sourcePath)
        }

        return sourceFiles
            .flatMap(::detectInFile)
    }

    private fun findKotlinFiles(sourceDirectory: Path): List<Path> =
        Files.walk(sourceDirectory).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .filter { path ->
                    path.fileName
                        .toString()
                        .endsWith(".kt")
                }
                .sorted()
                .toList()
        }

    private fun detectInFile(sourceFile: Path): List<String> {
        val source = Files.readString(sourceFile)

        return DriftCli(
            architecture = architecture,
        ).detect(source)
    }
}