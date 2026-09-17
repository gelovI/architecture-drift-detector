package dev.archdrift.cli

import dev.archdrift.core.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import dev.archdrift.core.ForbiddenDependencyRule

class ArchitectureDefinitionParserTest {

    @Test
    fun `parses component declaration`() {
        val definition = """
            component domain dev.shop.domain
        """.trimIndent()

        val architecture = ArchitectureDefinitionParser()
            .parse(definition)

        assertEquals(
            listOf(
                Component(
                    name = "domain",
                    packagePrefix = "dev.shop.domain",
                ),
            ),
            architecture.components,
        )

        assertEquals(
            emptyList(),
            architecture.rules,
        )
    }

    @Test
    fun `parses forbidden dependency rule`() {
        val definition = """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
    """.trimIndent()

        val architecture = ArchitectureDefinitionParser()
            .parse(definition)

        val domain = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        val infrastructure = Component(
            name = "infrastructure",
            packagePrefix = "dev.shop.infrastructure",
        )

        assertEquals(
            listOf(
                domain,
                infrastructure,
            ),
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