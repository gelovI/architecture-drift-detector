package dev.archdrift.analyzer.kotlin

import dev.archdrift.core.Dependency

class KotlinSourceAnalyzer {

    fun analyze(source: String): List<Dependency> {
        val packageName = PACKAGE_REGEX
            .find(source)
            ?.groupValues
            ?.get(1)
            ?: return emptyList()

        val className = CLASS_REGEX
            .find(source)
            ?.groupValues
            ?.get(1)
            ?: return emptyList()

        val qualifiedClassName = "$packageName.$className"

        val sourceWithoutImports = IMPORT_REGEX.replace(source, "")

        return IMPORT_REGEX
            .findAll(source)
            .map { match ->
                match.groupValues[1]
            }
            .filter { importedType ->
                val simpleName = importedType.substringAfterLast(".")

                Regex("""\b${Regex.escape(simpleName)}\b""")
                    .containsMatchIn(sourceWithoutImports)
            }
            .map { importedType ->
                Dependency(
                    source = qualifiedClassName,
                    target = importedType,
                )
            }
            .toList()
    }

    private companion object {
        val PACKAGE_REGEX = Regex(
            """(?m)^\s*package\s+([\w.]+)"""
        )

        val IMPORT_REGEX = Regex(
            """(?m)^\s*import\s+([\w.]+)"""
        )

        val CLASS_REGEX = Regex(
            """\bclass\s+(\w+)"""
        )
    }
}