package dev.archdrift.cli

import kotlin.test.Test
import kotlin.test.assertEquals

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

        val cli = DriftCli()

        val violations = cli.detect(source)

        assertEquals(
            listOf(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }
}