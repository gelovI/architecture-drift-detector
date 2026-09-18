package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ViolationIdentityTest {

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
    fun `same violation has same identity when source location changes`() {
        val first = Violation(
            rule = rule,
            dependency = Dependency(
                source = "dev.shop.domain.order.OrderService",
                target = "dev.shop.infrastructure.Database",
                location = SourceLocation(
                    file = "src/main/kotlin/OrderService.kt",
                    line = 6,
                ),
            ),
        )

        val second = Violation(
            rule = rule,
            dependency = Dependency(
                source = "dev.shop.domain.order.OrderService",
                target = "dev.shop.infrastructure.Database",
                location = SourceLocation(
                    file = "moved/OrderService.kt",
                    line = 42,
                ),
            ),
        )

        assertEquals(
            first.identity(),
            second.identity(),
        )
    }

    @Test
    fun `different dependency source has different identity`() {
        val first = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val second = violation(
            source = "dev.shop.domain.customer.CustomerService",
            target = "dev.shop.infrastructure.Database",
        )

        assertNotEquals(
            first.identity(),
            second.identity(),
        )
    }

    @Test
    fun `different dependency target has different identity`() {
        val first = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val second = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.MessageBroker",
        )

        assertNotEquals(
            first.identity(),
            second.identity(),
        )
    }

    @Test
    fun `different forbidden dependency rule has different identity`() {
        val otherInfrastructure = Component(
            name = "external",
            packagePrefix = "dev.shop.infrastructure",
        )

        val otherRule = ForbiddenDependencyRule(
            from = domain,
            to = otherInfrastructure,
        )

        val first = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val second = Violation(
            rule = otherRule,
            dependency = first.dependency,
        )

        assertNotEquals(
            first.identity(),
            second.identity(),
        )
    }

    private fun violation(
        source: String,
        target: String,
    ): Violation =
        Violation(
            rule = rule,
            dependency = Dependency(
                source = source,
                target = target,
            ),
        )
}