package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComponentTest {

    @Test
    fun `component matches class inside its package`() {
        val component = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        assertTrue(
            component.contains("dev.shop.domain.order.OrderService"),
        )
    }

    @Test
    fun `component does not match class outside its package`() {
        val component = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        assertFalse(
            component.contains("dev.shop.infrastructure.Database"),
        )
    }

    @Test
    fun `component does not match package with same textual prefix`() {
        val component = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        assertFalse(
            component.contains("dev.shop.domainlegacy.LegacyService"),
        )
    }
}