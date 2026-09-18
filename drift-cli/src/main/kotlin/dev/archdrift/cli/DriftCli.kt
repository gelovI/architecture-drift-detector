package dev.archdrift.cli

import dev.archdrift.analyzer.kotlin.KotlinSourceAnalyzer
import dev.archdrift.core.Architecture
import dev.archdrift.core.DriftDetector

class DriftCli(
    private val architecture: Architecture,
) {

    fun detect(
        source: String,
        sourceFile: String? = null,
    ): List<String> {
        val dependencies = KotlinSourceAnalyzer()
            .analyze(
                source = source,
                sourceFile = sourceFile,
            )

        return DriftDetector()
            .detect(
                dependencies = dependencies,
                architecture = architecture,
            )
            .map { violation ->
                val dependency = violation.dependency
                val location = dependency.location

                if (location == null) {
                    "${dependency.source} -> ${dependency.target}"
                } else {
                    "${location.file}:${location.line}: " +
                            "${dependency.source} -> ${dependency.target}"
                }
            }
    }
}