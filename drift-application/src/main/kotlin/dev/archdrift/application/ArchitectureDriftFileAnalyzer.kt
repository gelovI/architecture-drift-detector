package dev.archdrift.application

import dev.archdrift.core.Architecture
import dev.archdrift.core.Violation
import java.nio.file.Files
import java.nio.file.Path

class ArchitectureDriftFileAnalyzer(
    private val architecture: Architecture,
) {

    fun detect(sourcePath: Path): List<Violation> {
        val sourceFiles = if (Files.isDirectory(sourcePath)) {
            findKotlinFiles(sourcePath)
        } else {
            listOf(sourcePath)
        }

        return sourceFiles.flatMap(::detectInFile)
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

    private fun detectInFile(sourceFile: Path): List<Violation> {
        val source = Files.readString(sourceFile)

        return ArchitectureDriftAnalyzer(
            architecture = architecture,
        ).detect(
            source = source,
            sourceFile = sourceFile.toString(),
        )
    }
}