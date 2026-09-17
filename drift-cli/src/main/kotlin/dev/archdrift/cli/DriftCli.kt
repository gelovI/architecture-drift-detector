package dev.archdrift.cli

import dev.archdrift.analyzer.kotlin.KotlinSourceAnalyzer
import dev.archdrift.core.Architecture
import dev.archdrift.core.DriftDetector

class DriftCli(
    private val architecture: Architecture,
) {

    fun detect(source: String): List<String> {
        val dependencies = KotlinSourceAnalyzer().analyze(source)

        return DriftDetector()
            .detect(
                dependencies = dependencies,
                architecture = architecture,
            )
            .map { violation ->
                "${violation.dependency.source} -> ${violation.dependency.target}"
            }
    }
}