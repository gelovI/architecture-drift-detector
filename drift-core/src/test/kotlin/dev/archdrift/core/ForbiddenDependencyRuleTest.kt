package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ForbiddenDependencyRuleTest {

    private val domain = Component(
        name = "domain",
        packagePrefix = "dev.shop.domain",
    )

    private val infrastructure = Component(
        name = "infrastructure",
        packagePrefix = "dev.shop.infrastructure",
    )

    private val rule = ForbiddenDependencyRule(
        from = domain,
        to = infrastructure,
    )

    @Test
    fun `rule is violated when forbidden dependency exists`() {
        val dependency = Dependency(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        assertTrue(rule.isViolatedBy(dependency))
    }

    @Test
    fun `rule is not violated when dependency points in allowed direction`() {
        val dependency = Dependency(
            source = "dev.shop.infrastructure.Database",
            target = "dev.shop.domain.order.Order",
        )

        assertFalse(rule.isViolatedBy(dependency))
    }

    @Test
    fun `rule is not violated by dependency inside same component`() {
        val dependency = Dependency(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.domain.order.Order",
        )

        assertFalse(rule.isViolatedBy(dependency))
    }
}