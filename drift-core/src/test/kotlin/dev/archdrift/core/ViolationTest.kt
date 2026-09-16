package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationTest {

    @Test
    fun `violation identifies rule and offending dependency`() {
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
        val dependency = Dependency(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val violation = Violation(
            rule = rule,
            dependency = dependency,
        )

        assertEquals(rule, violation.rule)
        assertEquals(dependency, violation.dependency)
    }
}