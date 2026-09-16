package dev.archdrift.core

class DriftDetector {

    fun detect(
        dependencies: List<Dependency>,
        rules: List<ArchitectureRule>,
    ): List<Violation> =
        rules.flatMap { rule ->
            dependencies
                .filter(rule::isViolatedBy)
                .map { dependency ->
                    Violation(
                        rule = rule,
                        dependency = dependency,
                    )
                }
        }
}