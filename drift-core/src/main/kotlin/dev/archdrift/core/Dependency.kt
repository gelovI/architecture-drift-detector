package dev.archdrift.core

data class Dependency(
    val source: String,
    val target: String,
    val location: SourceLocation? = null,
)