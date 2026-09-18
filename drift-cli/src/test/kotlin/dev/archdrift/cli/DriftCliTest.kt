package dev.archdrift.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule

class DriftCliTest {

    @Test
    fun `detects forbidden architecture dependency from Kotlin source`() {
        val source = """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService(
                private val database: Database,
            )
        """.trimIndent()

        val domain = Component(
            name = "domain",
            packagePrefix = "dev.shop.domain",
        )

        val infrastructure = Component(
            name = "infrastructure",
            packagePrefix = "dev.shop.infrastructure",
        )

        val architecture = Architecture(
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

        val cli = DriftCli(
            architecture = architecture,
        )

        val violations = cli.detect(source)

        assertEquals(
            listOf(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }

    @Test
    fun `preserves source location in detected violation`() {
        val source = """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService(
            private val database: Database,
        )
    """.trimIndent()

        val violations = DriftCli(
            architecture = testArchitecture(),
        ).detect(
            source = source,
            sourceFile = "src/main/kotlin/dev/shop/domain/order/OrderService.kt",
        )

        assertEquals(
            listOf(
                "src/main/kotlin/dev/shop/domain/order/OrderService.kt:6: " +
                        "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }
}