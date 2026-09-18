package dev.archdrift.cli

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CliApplicationTest {

    @Test
    fun `returns usage when no file argument is provided`() {
        val application = CliApplication()

        val result = application.run(emptyArray())

        assertEquals(
            listOf(
                "Usage: architecture-drift-detector <architecture-file> <kotlin-file-or-directory>",            ),
            result.output,
        )
    }

    @Test
    fun `detects architecture drift for provided Kotlin file`() {
        val architectureFile = testArchitectureFile()

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

        val result = application.run(
            arrayOf(
                architectureFile.toString(),
                sourceFile.toString(),
            ),
        )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "${sourceFile}:6: " +
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
        val architectureFile = testArchitectureFile()

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

        val result = application.run(
            arrayOf(
                architectureFile.toString(),
                sourceFile.toString(),
            ),
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
        val architectureFile = testArchitectureFile()

        val application = CliApplication()

        val result = application.run(
            arrayOf(
                architectureFile.toString(),
                "does-not-exist.kt",
            ),
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
    fun `reports error when input file is not a Kotlin source file`() {
        val architectureFile = testArchitectureFile()

        val sourceFile = Files.createTempFile(
            "architecture-drift-",
            ".txt",
        )

        val application = CliApplication()

        val result = application.run(
            arrayOf(
                architectureFile.toString(),
                sourceFile.toString(),
            ),
        )

        assertEquals(
            listOf(
                "Error: Input file must be a Kotlin source file: $sourceFile",
            ),
            result.output,
        )
    }

    @Test
    fun `loads architecture from provided architecture file`() {
        val architectureFile = Files.createTempFile(
            "architecture",
            ".drift",
        )

        Files.writeString(
            architectureFile,
            """
                component business com.acme.business
                component persistence com.acme.persistence
                forbid business -> persistence
            """.trimIndent(),
        )

        val sourceFile = Files.createTempFile(
            "OrderService",
            ".kt",
        )

        Files.writeString(
            sourceFile,
            """
                package com.acme.business.order

                import com.acme.persistence.Database

                class OrderService(
                    private val database: Database,
                )
            """.trimIndent(),
        )

        val application = CliApplication()

        val result = application.run(
            arrayOf(
                architectureFile.toString(),
                sourceFile.toString(),
            ),
        )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "${sourceFile}:6: " +
                        "com.acme.business.order.OrderService -> com.acme.persistence.Database",
            ),
            result.output,
        )

        assertEquals(
            1,
            result.exitCode,
        )
    }

    @Test
    fun `rejects missing architecture file`() {
        val architectureFile = Path.of("missing-architecture.drift")
        val sourceFile = Files.createTempFile(
            "OrderService",
            ".kt",
        )

        Files.writeString(
            sourceFile,
            """
            package dev.shop.domain.order

            class OrderService
        """.trimIndent(),
        )

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureFile.toString(),
                    sourceFile.toString(),
                ),
            )

        assertEquals(
            listOf(
                "Error: Architecture file does not exist: $architectureFile",
            ),
            result.output,
        )
        assertEquals(2, result.exitCode)
    }

    @Test
    fun `rejects architecture path that is not a file`() {
        val architectureDirectory = Files.createTempDirectory(
            "architecture",
        )
        val sourceFile = Files.createTempFile(
            "OrderService",
            ".kt",
        )

        Files.writeString(
            sourceFile,
            """
            package dev.shop.domain.order

            class OrderService
        """.trimIndent(),
        )

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureDirectory.toString(),
                    sourceFile.toString(),
                ),
            )

        assertEquals(
            listOf(
                "Error: Architecture path is not a file: $architectureDirectory",
            ),
            result.output,
        )
        assertEquals(2, result.exitCode)
    }

    @Test
    fun `rejects invalid architecture definition`() {
        val architectureFile = Files.createTempFile(
            "architecture",
            ".drift",
        )
        val sourceFile = Files.createTempFile(
            "OrderService",
            ".kt",
        )

        Files.writeString(
            architectureFile,
            """
            component domain
        """.trimIndent(),
        )

        Files.writeString(
            sourceFile,
            """
            package dev.shop.domain.order

            class OrderService
        """.trimIndent(),
        )

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureFile.toString(),
                    sourceFile.toString(),
                ),
            )

        assertEquals(
            listOf(
                "Error: Invalid architecture definition: Invalid component declaration: component domain",
            ),
            result.output,
        )
        assertEquals(2, result.exitCode)
    }

    @Test
    fun `detects architecture drift in source directory`() {
        val architectureFile = testArchitectureFile()
        val sourceDirectory = Files.createTempDirectory("sources")

        val nestedDirectory = Files.createDirectories(
            sourceDirectory.resolve("dev/shop/domain/order"),
        )

        val orderServiceFile =
            nestedDirectory.resolve("OrderService.kt")

        Files.writeString(
            orderServiceFile,
            """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService(
                private val database: Database,
            )
        """.trimIndent(),
        )

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureFile.toString(),
                    sourceDirectory.toString(),
                ),
            )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "${orderServiceFile}:6: " +
                        "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            result.output,
        )
        assertEquals(1, result.exitCode)
    }

    @Test
    fun `accepts clean source directory`() {
        val architectureFile = testArchitectureFile()
        val sourceDirectory = Files.createTempDirectory("sources")

        Files.writeString(
            sourceDirectory.resolve("OrderService.kt"),
            """
            package dev.shop.domain.order

            class OrderService
        """.trimIndent(),
        )

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureFile.toString(),
                    sourceDirectory.toString(),
                ),
            )

        assertEquals(
            listOf(
                "No architecture drift detected.",
            ),
            result.output,
        )
        assertEquals(0, result.exitCode)
    }
}