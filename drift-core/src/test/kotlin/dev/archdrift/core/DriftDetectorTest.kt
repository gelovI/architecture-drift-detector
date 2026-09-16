package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DriftDetectorTest {

    @Test
    fun `detects forbidden architecture dependency`() {
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

        val forbiddenDependency = Dependency(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val allowedDependency = Dependency(
            source = "dev.shop.infrastructure.Database",
            target = "dev.shop.domain.order.Order",
        )

        val detector = DriftDetector()

        val violations = detector.detect(
            dependencies = listOf(
                forbiddenDependency,
                allowedDependency,
            ),
            rules = listOf(rule),
        )

        assertEquals(
            listOf(
                Violation(
                    rule = rule,
                    dependency = forbiddenDependency,
                ),
            ),
            violations,
        )
    }
}