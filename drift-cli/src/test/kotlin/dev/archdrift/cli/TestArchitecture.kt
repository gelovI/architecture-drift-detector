package dev.archdrift.cli

import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule
import java.nio.file.Files
import java.nio.file.Path

fun testArchitecture(): Architecture {
    val domain = Component(
        name = "domain",
        packagePrefix = "dev.shop.domain",
    )

    val infrastructure = Component(
        name = "infrastructure",
        packagePrefix = "dev.shop.infrastructure",
    )

    return Architecture(
        components = listOf(
            domain,
            infrastructure,
        ),
        rules = listOf(
            ForbiddenDependencyRule(
                from = domain,
                to = infrastructure,
            ),
        ),
    )
}

fun testArchitectureFile(): Path {
    val architectureFile = Files.createTempFile(
        "architecture",
        ".drift",
    )

    Files.writeString(
        architectureFile,
        """
            component domain dev.shop.domain
            component infrastructure dev.shop.infrastructure
            forbid domain -> infrastructure
        """.trimIndent(),
    )

    return architectureFile
}