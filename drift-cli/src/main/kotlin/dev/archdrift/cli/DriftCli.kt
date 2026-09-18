package dev.archdrift.cli

import dev.archdrift.application.ArchitectureDriftAnalyzer
import dev.archdrift.core.Architecture
import dev.archdrift.core.ForbiddenDependencyRule

class DriftCli(
    private val architecture: Architecture,
) {

    fun detect(
        source: String,
        sourceFile: String? = null,
    ): List<String> =
        ArchitectureDriftAnalyzer(
            architecture = architecture,
        )
            .detect(
                source = source,
                sourceFile = sourceFile,
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