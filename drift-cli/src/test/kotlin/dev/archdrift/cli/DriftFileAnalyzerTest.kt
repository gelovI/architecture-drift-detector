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
}