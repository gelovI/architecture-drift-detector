package dev.archdrift.cli

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class DriftFileAnalyzerTest {

    @Test
    fun `detects architecture drift from Kotlin file`() {
        val sourceFile = Files.createTempFile(
            "architecture-drift-",
            ".kt",
        )

        Files.writeString(
            sourceFile,
            """
                package dev.shop.domain.order

                import dev.shop.infrastructure.Database

                class OrderService(
                    private val database: Database,
                )
            """.trimIndent(),
        )

        val analyzer = DriftFileAnalyzer(
            architecture = testArchitecture(),
        )

        val violations = DriftFileAnalyzer(
            architecture = testArchitecture(),
        ).detect(sourceFile)

        assertEquals(
            listOf(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }

    @Test
    fun `detects drift across Kotlin files in directory`() {
        val sourceDirectory = Files.createTempDirectory("sources")

        val domainDirectory = Files.createDirectories(
            sourceDirectory.resolve("dev/shop/domain/order"),
        )

        Files.writeString(
            domainDirectory.resolve("OrderService.kt"),
            """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService(
                private val database: Database,
            )
        """.trimIndent(),
        )

        Files.writeString(
            domainDirectory.resolve("Order.kt"),
            """
            package dev.shop.domain.order

            class Order
        """.trimIndent(),
        )

        val violations = DriftFileAnalyzer(
            architecture = testArchitecture(),
        ).detect(sourceDirectory)

        assertEquals(
            listOf(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }

    @Test
    fun `analyzes Kotlin files recursively`() {
        val sourceDirectory = Files.createTempDirectory("sources")

        val nestedDirectory = Files.createDirectories(
            sourceDirectory.resolve("deeply/nested/source"),
        )

        Files.writeString(
            nestedDirectory.resolve("OrderService.kt"),
            """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService(
                private val database: Database,
            )
        """.trimIndent(),
        )

        val violations = DriftFileAnalyzer(
            architecture = testArchitecture(),
        ).detect(sourceDirectory)

        assertEquals(
            listOf(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            violations,
        )
    }
}