package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBaselineTest {

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
    fun `empty baseline keeps all violations as new`() {
        val violations = listOf(
            violation(
                source = "dev.shop.domain.order.OrderService",
                target = "dev.shop.infrastructure.Database",
            ),
            violation(
                source = "dev.shop.domain.customer.CustomerService",
                target = "dev.shop.infrastructure.MessageBroker",
            ),
        )

        val baseline = ViolationBaseline(
            identities = emptySet(),
        )

        assertEquals(
            violations,
            baseline.newViolations(violations),
        )
    }

    @Test
    fun `baselined violation is not classified as new`() {
        val known = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val new = violation(
            source = "dev.shop.domain.customer.CustomerService",
            target = "dev.shop.infrastructure.MessageBroker",
        )

        val baseline = ViolationBaseline(
            identities = setOf(known.identity()),
        )

        assertEquals(
            listOf(new),
            baseline.newViolations(
                listOf(known, new),
            ),
        )
    }

    @Test
    fun `source location changes do not make baselined violation new`() {
        val baselined = Violation(
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

        val moved = Violation(
            rule = rule,
            dependency = Dependency(
                source = "dev.shop.domain.order.OrderService",
                target = "dev.shop.infrastructure.Database",
                location = SourceLocation(
                    file = "src/main/kotlin/order/OrderService.kt",
                    line = 42,
                ),
            ),
        )

        val baseline = ViolationBaseline(
            identities = setOf(baselined.identity()),
        )

        assertTrue(
            baseline.newViolations(listOf(moved)).isEmpty(),
        )
    }

    @Test
    fun `stale baseline entries do not create violations`() {
        val stale = violation(
            source = "dev.shop.domain.legacy.LegacyService",
            target = "dev.shop.infrastructure.LegacyDatabase",
        )

        val baseline = ViolationBaseline(
            identities = setOf(stale.identity()),
        )

        assertTrue(
            baseline.newViolations(emptyList()).isEmpty(),
        )
    }

    @Test
    fun `new violations preserve detection order`() {
        val first = violation(
            source = "dev.shop.domain.order.OrderService",
            target = "dev.shop.infrastructure.Database",
        )

        val known = violation(
            source = "dev.shop.domain.legacy.LegacyService",
            target = "dev.shop.infrastructure.LegacyDatabase",
        )

        val second = violation(
            source = "dev.shop.domain.customer.CustomerService",
            target = "dev.shop.infrastructure.MessageBroker",
        )

        val baseline = ViolationBaseline(
            identities = setOf(known.identity()),
        )

        assertEquals(
            listOf(first, second),
            baseline.newViolations(
                listOf(first, known, second),
            ),
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