package dev.archdrift.application

import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule
import dev.archdrift.core.ForbiddenDependencyRuleIdentity
import dev.archdrift.core.ViolationBaseline
import dev.archdrift.core.ViolationIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchitectureDriftAnalyzerTest {

    private val domain = Component(
        name = "domain",
        packagePrefix = "dev.shop.domain",
    )

    private val infrastructure = Component(
        name = "infrastructure",
        packagePrefix = "dev.shop.infrastructure",
    )

    private val architecture = Architecture(
        components = listOf(
            domain,
            infrastructure,
        ),
        rules = listOf(
            ForbiddenDependencyRule(
                from = domain,
                to = infrastructure,
            ),
        ),
    )

    @Test
    fun `baselined architecture drift is not returned as new`() {
        val baseline = ViolationBaseline(
            identities = setOf(
                ViolationIdentity(
                    rule = ForbiddenDependencyRuleIdentity(
                        from = "domain",
                        to = "infrastructure",
                    ),
                    source = "dev.shop.domain.order.OrderService",
                    target = "dev.shop.infrastructure.Database",
                ),
            ),
        )

        val violations = ArchitectureDriftAnalyzer(
            architecture = architecture,
        ).detectNew(
            source =
                """
                package dev.shop.domain.order

                import dev.shop.infrastructure.Database

                class OrderService {
                    private val database = Database()
                }
                """.trimIndent(),
            sourceFile = "OrderService.kt",
            baseline = baseline,
        )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `new architecture drift is returned when it is not in baseline`() {
        val baseline = ViolationBaseline(
            identities = emptySet(),
        )

        val violations = ArchitectureDriftAnalyzer(
            architecture = architecture,
        ).detectNew(
            source =
                """
                package dev.shop.domain.order

                import dev.shop.infrastructure.Database

                class OrderService {
                    private val database = Database()
                }
                """.trimIndent(),
            sourceFile = "OrderService.kt",
            baseline = baseline,
        )

        assertEquals(
            1,
            violations.size,
        )

        assertEquals(
            "dev.shop.domain.order.OrderService",
            violations.single().dependency.source,
        )

        assertEquals(
            "dev.shop.infrastructure.Database",
            violations.single().dependency.target,
        )
    }
}