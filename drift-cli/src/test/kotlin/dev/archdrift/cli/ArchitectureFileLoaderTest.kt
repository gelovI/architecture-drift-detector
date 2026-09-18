package dev.archdrift.cli

import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import dev.archdrift.application.ArchitectureFileLoader

class ArchitectureFileLoaderTest {

    @Test
    fun `loads architecture definition from file`() {
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

        val architecture = ArchitectureFileLoader()
            .load(architectureFile)

        val domain = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        val infrastructure = Component(
            name = "infrastructure",
            packagePrefix = "dev.shop.infrastructure",
        )

        assertEquals(
            listOf(domain, infrastructure),
            architecture.components,
        )

        assertEquals(
            listOf(
                ForbiddenDependencyRule(
                    from = domain,
                    to = infrastructure,
                ),
            ),
            architecture.rules,
        )
    }
}