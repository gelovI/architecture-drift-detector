package dev.archdrift.core

data class Component(
    val name: String,
    val packagePrefix: String,
) {
    fun contains(qualifiedClassName: String): Boolean =
        qualifiedClassName.startsWith("$packagePrefix.")
}