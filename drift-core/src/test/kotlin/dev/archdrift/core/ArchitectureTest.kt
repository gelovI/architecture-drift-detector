package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ArchitectureTest {

    @Test
    fun `stores components and architecture rules`() {
        val domain = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        val infrastructure = Component(
            name = "infrastructure",
            packagePrefix = "dev.shop.infrastructure",
        )

        val rule = ForbiddenDependencyRule(
            from = domain,
            to = infrastructure,
        )

        val architecture = Architecture(
            components = listOf(
                domain,
                infrastructure,
            ),
            rules = listOf(rule),
        )

        assertEquals(
            listOf(domain, infrastructure),
            architecture.components,
        )

        assertEquals(
            listOf(rule),
            architecture.rules,
        )
    }
}