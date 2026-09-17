package dev.archdrift.cli

import kotlin.test.Test
import kotlin.test.assertEquals

class CliResultTest {

    @Test
    fun `stores output and exit code`() {
        val result = CliResult(
            output = listOf("No architecture drift detected."),
            exitCode = 0,
        )

        assertEquals(
            listOf("No architecture drift detected."),
            result.output,
        )

        assertEquals(
            0,
            result.exitCode,
        )
    }
}