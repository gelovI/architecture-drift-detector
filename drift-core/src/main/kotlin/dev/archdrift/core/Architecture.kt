package dev.archdrift.core

data class Architecture(
    val components: List<Component>,
    val rules: List<ArchitectureRule>,
)