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

                val sourceComponentName = parts[1]
                val targetComponentName = parts[3]

                val sourceComponent = componentsByName[sourceComponentName]
                    ?: throw IllegalArgumentException(
                        "Unknown component in forbidden dependency rule: $sourceComponentName",
                    )

                val targetComponent = componentsByName[targetComponentName]
                    ?: throw IllegalArgumentException(
                        "Unknown component in forbidden dependency rule: $targetComponentName",
                    )

                ForbiddenDependencyRule(
                    from = sourceComponent,
                    to = targetComponent,
                )
            }

        return Architecture(
            components = components,
            rules = rules,
        )
    }
}