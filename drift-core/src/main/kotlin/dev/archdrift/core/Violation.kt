package dev.archdrift.core

data class Violation(
    val rule: ArchitectureRule,
    val dependency: Dependency,
)