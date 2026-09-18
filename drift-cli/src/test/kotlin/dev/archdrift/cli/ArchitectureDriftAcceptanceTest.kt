package dev.archdrift.cli

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class ArchitectureDriftAcceptanceTest {

    private val projectRoot: Path =
        Path.of("..")
            .toAbsolutePath()
            .normalize()

    @Test
    fun `detects architecture drift in broken example`() {
        val architectureFile =
            projectRoot.resolve("architecture.drift")

        val sourceFile =
            projectRoot.resolve("examples/broken/OrderService.kt")

        val result = CliApplication()
            .run(
                arrayOf(
                    architectureFile.toString(),
                    sourceFile.toString(),
                ),
            )

        assertEquals(
            listOf(
                "Architecture drift detected:",
                "${sourceFile}:6: " +
                        "forbidden dependency domain -> infrastructure: " +
                        "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            result.output,
        )

        assertEquals(1, result.exitCode)
    }

    @Test
    fun `accepts clean example`() {
        val architectureFile =
            projectRoot.resolve("architecture.drift")

        val sourceFile =
            projectRoot.resolve("examples/clean/OrderService.kt")

        val result = CliApplication()
            .run(
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

        assertEquals(0, result.exitCode)
    }
}