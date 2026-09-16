package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DependencyTest {

    @Test
    fun `dependency has a source and target`() {
        val dependency = Dependency(
            source = "dev.shop.domain.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        assertEquals(
            "dev.shop.domain.OrderService",
            dependency.source,
        )
        assertEquals(
            "dev.shop.infrastructure.Database",
            dependency.target,
        )
    }
}