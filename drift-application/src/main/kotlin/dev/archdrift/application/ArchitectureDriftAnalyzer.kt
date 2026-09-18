package dev.archdrift.application

import dev.archdrift.analyzer.kotlin.KotlinSourceAnalyzer
import dev.archdrift.core.Architecture
import dev.archdrift.core.DriftDetector
import dev.archdrift.core.Violation

class ArchitectureDriftAnalyzer(
    private val architecture: Architecture,
) {

    fun detect(
        source: String,
        sourceFile: String? = null,
    ): List<Violation> {
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
            .sortedWith(
                compareBy(
                    { it.dependency.location?.file ?: "" },
                    { it.dependency.location?.line ?: Int.MAX_VALUE },
                    { it.dependency.source },
                    { it.dependency.target },
                ),
            )
    }
}