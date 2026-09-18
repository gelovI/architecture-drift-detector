package dev.archdrift.cli

import dev.archdrift.analyzer.kotlin.KotlinSourceAnalyzer
import dev.archdrift.core.Architecture
import dev.archdrift.core.DriftDetector
import dev.archdrift.core.ForbiddenDependencyRule

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
            .sortedWith(
                compareBy(
                    { it.dependency.location?.file ?: "" },
                    { it.dependency.location?.line ?: Int.MAX_VALUE },
                    { it.dependency.source },
                    { it.dependency.target },
                ),
            )
            .map { violation ->
                val dependency = violation.dependency
                val location = dependency.location

                val ruleDescription = when (val rule = violation.rule) {
                    is ForbiddenDependencyRule ->
                        "forbidden dependency ${rule.from.name} -> ${rule.to.name}"

                    else ->
                        "architecture rule violation"
                }

                val dependencyDescription =
                    "${dependency.source} -> ${dependency.target}"

                if (location == null) {
                    "$ruleDescription: $dependencyDescription"
                } else {
                    "${location.file}:${location.line}: " +
                            "$ruleDescription: $dependencyDescription"
                }
            }
    }
}