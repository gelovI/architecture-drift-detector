package dev.archdrift.core

interface ArchitectureRule {

    fun isViolatedBy(dependency: Dependency): Boolean
}