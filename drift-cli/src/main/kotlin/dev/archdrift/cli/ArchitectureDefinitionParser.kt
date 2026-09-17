package dev.archdrift.cli

import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule

class ArchitectureDefinitionParser {

    fun parse(definition: String): Architecture {
        val lines = definition
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val components = lines
            .filter { it.startsWith("component ") }
            .map { line ->
                val parts = line.split(Regex("\\s+"))

                Component(
                    name = parts[1],
                    packagePrefix = parts[2],
                )
            }

        val componentsByName = components
            .associateBy { it.name }

        val rules = lines
            .filter { it.startsWith("forbid ") }
            .map { line ->
                val parts = line.split(Regex("\\s+"))

                ForbiddenDependencyRule(
                    from = componentsByName.getValue(parts[1]),
                    to = componentsByName.getValue(parts[3]),
                )
            }

        return Architecture(
            components = components,
            rules = rules,
        )
    }
}