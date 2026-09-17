package dev.archdrift.cli

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class CliApplicationTest {

    @Test
    fun `returns usage when no file argument is provided`() {
        val application = CliApplication()

        val output = application.run(emptyArray())

        assertEquals(
            listOf("Usage: architecture-drift-detector <kotlin-file>"),
            output,
        )
    }

    @Test
    fun `detects architecture drift for provided Kotlin file`() {
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

        val application = CliApplication()

        val output = application.run(
            arrayOf(sourceFile.toString()),
        )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            output,
        )
    }

    @Test
    fun `reports no architecture drift for allowed Kotlin file`() {
        val sourceFile = Files.createTempFile(
            "architecture-drift-",
            ".kt",
        )

        Files.writeString(
            sourceFile,
            """
            package dev.shop.domain.order

            class OrderService
        """.trimIndent(),
        )

        val application = CliApplication()

        val output = application.run(
            arrayOf(sourceFile.toString()),
        )

        assertEquals(
            listOf(
                "No architecture drift detected.",
            ),
            output,
        )
    }

    @Test
    fun `reports error when Kotlin file does not exist`() {
        val application = CliApplication()

        val output = application.run(
            arrayOf("does-not-exist.kt"),
        )

        assertEquals(
            listOf(
                "Error: Kotlin file does not exist: does-not-exist.kt",
            ),
            output,
        )
    }
}