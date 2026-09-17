package dev.archdrift.cli

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class CliApplicationTest {

    @Test
    fun `returns usage when no file argument is provided`() {
        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(emptyArray())

        assertEquals(
            listOf("Usage: architecture-drift-detector <kotlin-file>"),
            result.output,
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

        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(
            arrayOf(sourceFile.toString()),
        )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            result.output,
        )

        assertEquals(
            1,
            result.exitCode,
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

        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(
            arrayOf(sourceFile.toString()),
        )

        assertEquals(
            listOf(
                "No architecture drift detected.",
            ),
            result.output,
        )

        assertEquals(
            0,
            result.exitCode,
        )
    }

    @Test
    fun `reports error when Kotlin file does not exist`() {
        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(
            arrayOf("does-not-exist.kt"),
        )

        assertEquals(
            listOf(
                "Error: Kotlin file does not exist: does-not-exist.kt",
            ),
            result.output,
        )

        assertEquals(
            2,
            result.exitCode,
        )
    }

    @Test
    fun `reports error when input path is not a file`() {
        val sourceDirectory = Files.createTempDirectory(
            "architecture-drift-",
        )

        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(
            arrayOf(sourceDirectory.toString()),
        )

        assertEquals(
            listOf(
                "Error: Input path is not a file: $sourceDirectory",
            ),
            result.output,
        )
    }

    @Test
    fun `reports error when input file is not a Kotlin source file`() {
        val sourceFile = Files.createTempFile(
            "architecture-drift-",
            ".txt",
        )

        val application = CliApplication(
            architecture = testArchitecture(),
        )

        val result = application.run(
            arrayOf(sourceFile.toString()),
        )

        assertEquals(
            listOf(
                "Error: Input file must be a Kotlin source file: $sourceFile",
            ),
            result.output,
        )
    }
}