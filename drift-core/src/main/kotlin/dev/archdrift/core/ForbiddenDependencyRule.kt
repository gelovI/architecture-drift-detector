package dev.archdrift.core

data class ForbiddenDependencyRule(
    val from: Component,
    val to: Component,
) : ArchitectureRule {
    override fun isViolatedBy(dependency: Dependency): Boolean =
        from.contains(dependency.source) &&
                to.contains(dependency.target)
}