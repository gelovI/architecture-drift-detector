package dev.archdrift.application

import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule

class ArchitectureDefinitionParser {

    fun parse(definition: String): Architecture {
        val lines = definition
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) {
            throw IllegalArgumentException(
                "Architecture definition must not be empty",
            )
        }

        validateStatements(lines)

        val components = lines
            .filter { it.startsWith("component ") }
            .map(::parseComponent)

        val duplicateComponentName = components
            .groupingBy { it.name }
            .eachCount()
            .entries
            .firstOrNull { (_, count) -> count > 1 }
            ?.key

        if (duplicateComponentName != null) {
            throw IllegalArgumentException(
                "Duplicate component: $duplicateComponentName",
            )
        }

        val componentsByName = components
            .associateBy { it.name }

        val rules = lines
            .filter { it.startsWith("forbid ") }
            .map { line ->
                parseForbiddenDependencyRule(
                    line = line,
                    componentsByName = componentsByName,
                )
            }

        return Architecture(
            components = components,
            rules = rules,
        )
    }

    private fun validateStatements(lines: List<String>) {
        lines.forEach { line ->
            if (
                !line.startsWith("component ") &&
                !line.startsWith("forbid ")
            ) {
                throw IllegalArgumentException(
                    "Unknown architecture statement: $line",
                )
            }
        }
    }

    private fun parseComponent(line: String): Component {
        val parts = line.split(Regex("\\s+"))

        if (parts.size != 3) {
            throw IllegalArgumentException(
                "Invalid component declaration: $line",
            )
        }

        return Component(
            name = parts[1],
            packagePrefix = parts[2],
        )
    }

    private fun parseForbiddenDependencyRule(
        line: String,
        componentsByName: Map<String, Component>,
    ): ForbiddenDependencyRule {
        val parts = line.split(Regex("\\s+"))

        if (
            parts.size != 4 ||
            parts[2] != "->"
        ) {
            throw IllegalArgumentException(
                "Invalid forbidden dependency rule: $line",
            )
        }

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

        return ForbiddenDependencyRule(
            from = sourceComponent,
            to = targetComponent,
        )
    }
}