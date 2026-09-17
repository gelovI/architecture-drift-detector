package dev.archdrift.cli

import dev.archdrift.analyzer.kotlin.KotlinSourceAnalyzer
import dev.archdrift.core.Component
import dev.archdrift.core.DriftDetector
import dev.archdrift.core.ForbiddenDependencyRule

class DriftCli {

    fun detect(source: String): List<String> {
        val dependencies = KotlinSourceAnalyzer().analyze(source)

        val domain = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        val infrastructure = Component(
            name = "infrastructure",
            packagePrefix = "dev.shop.infrastructure",
        )

        val rule = ForbiddenDependencyRule(
            from = domain,
            to = infrastructure,
        )

        return DriftDetector()
            .detect(
                dependencies = dependencies,
                rules = listOf(rule),
            )
            .map { violation ->
                "${violation.dependency.source} -> ${violation.dependency.target}"
            }
    }
}