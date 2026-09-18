package dev.archdrift.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertTrue

class ArchitectureDriftPluginTest {

    @Test
    fun `registers architecture drift check task`() {
        val projectDir = Files.createTempDirectory("architecture-drift-plugin-test")

        Files.writeString(
            projectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("build.gradle.kts"),
            """
            plugins {
                id("dev.archdrift")
            }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assertTrue(
            result.output.contains("architectureDriftCheck"),
            result.output,
        )
    }
}