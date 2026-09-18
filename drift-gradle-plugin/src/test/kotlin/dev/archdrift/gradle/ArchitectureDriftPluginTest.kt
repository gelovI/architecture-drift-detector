package dev.archdrift.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.assertEquals

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

    @Test
    fun `architecture drift check succeeds for clean project`() {
        val projectDir = Files.createTempDirectory("architecture-drift-clean")

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

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        class OrderService
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":architectureDriftCheck")?.outcome,
        )
    }

    @Test
    fun `architecture drift check fails for forbidden dependency`() {
        val projectDir = Files.createTempDirectory("architecture-drift-broken")

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

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService(
            private val database: Database,
        )
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains("Architecture drift detected"),
            result.output,
        )

        assertTrue(
            result.output.contains(
                "forbidden dependency domain -> infrastructure",
            ),
            result.output,
        )

        assertTrue(
            result.output.contains(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            result.output,
        )
    }

    @Test
    fun `check depends on architecture drift check`() {
        val projectDir = Files.createTempDirectory("architecture-drift-check-lifecycle")

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
            java
            id("dev.archdrift")
        }
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService(
            private val database: Database,
        )
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("check")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":architectureDriftCheck")?.outcome,
        )

        assertTrue(
            result.output.contains("Architecture drift detected"),
            result.output,
        )
    }

    @Test
    fun `uses configured architecture file`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-custom-architecture",
        )

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

        architectureDrift {
            architectureFile.set(
                layout.projectDirectory.file("config/custom.drift")
            )
        }
        """.trimIndent(),
        )

        val configDir = projectDir.resolve("config")
        Files.createDirectories(configDir)

        Files.writeString(
            configDir.resolve("custom.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        class OrderService
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":architectureDriftCheck")?.outcome,
        )
    }

    @Test
    fun `uses configured source directory`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-custom-source",
        )

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

        architectureDrift {
            sourceDirectory.set(
                layout.projectDirectory.dir("custom-sources")
            )
        }
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "custom-sources/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService(
            private val database: Database,
        )
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":architectureDriftCheck")?.outcome,
        )

        assertTrue(
            result.output.contains("Architecture drift detected"),
            result.output,
        )

        assertTrue(
            result.output.contains(
                "dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database",
            ),
            result.output,
        )
    }

    @Test
    fun `fails with clear message when architecture file does not exist`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-missing-architecture",
        )

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

        val sourceDir = projectDir.resolve("src/main/kotlin")
        Files.createDirectories(sourceDir)

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains(
                "property 'architectureFile' specifies file",
            ),
            result.output,
        )

        assertTrue(
            result.output.contains(
                "An input file was expected to be present but it doesn't exist.",
            ),
            result.output,
        )
    }

    @Test
    fun `fails with clear message when source directory does not exist`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-missing-source",
        )

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

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains(
                "property 'sourceDirectory' specifies directory",
            ),
            result.output,
        )

        assertTrue(
            result.output.contains(
                "An input file was expected to be present but it doesn't exist.",
            ),
            result.output,
        )
    }

    @Test
    fun `architecture drift check is up to date when inputs do not change`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-up-to-date",
        )

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

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        class OrderService
        """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.UP_TO_DATE,
            secondResult.task(":architectureDriftCheck")?.outcome,
        )
    }

    @Test
    fun `configured baseline tolerates existing architecture drift`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-baseline-existing",
        )

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

        architectureDrift {
            baselineFile.set(
                layout.projectDirectory.file("architecture-drift.baseline")
            )
        }
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture-drift.baseline"),
            "forbid domain -> infrastructure | " +
                    "dev.shop.domain.order.OrderService -> " +
                    "dev.shop.infrastructure.Database",
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService(
            private val database: Database,
        )
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":architectureDriftCheck")?.outcome,
        )

        assertTrue(
            Files.readString(
                projectDir.resolve(
                    "build/architecture-drift/check-result.txt",
                ),
            ).contains("No new architecture drift detected."),
        )
    }

    @Test
    fun `configured baseline does not tolerate new architecture drift`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-baseline-new",
        )

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

        architectureDrift {
            baselineFile.set(
                layout.projectDirectory.file("architecture-drift.baseline")
            )
        }
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture-drift.baseline"),
            "forbid domain -> infrastructure | " +
                    "dev.shop.domain.order.OrderService -> " +
                    "dev.shop.infrastructure.Database",
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/customer",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("CustomerService.kt"),
            """
        package dev.shop.domain.customer

        import dev.shop.infrastructure.MessageBroker

        class CustomerService(
            private val messageBroker: MessageBroker,
        )
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":architectureDriftCheck")?.outcome,
        )

        assertTrue(
            result.output.contains(
                "dev.shop.domain.customer.CustomerService -> " +
                        "dev.shop.infrastructure.MessageBroker",
            ),
            result.output,
        )
    }

    @Test
    fun `architecture drift check works without configured baseline`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-no-baseline",
        )

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

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        class OrderService
        """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":architectureDriftCheck")?.outcome,
        )
    }

    @Test
    fun `architecture drift check reruns when baseline changes`() {
        val projectDir = Files.createTempDirectory(
            "architecture-drift-baseline-input",
        )

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

        architectureDrift {
            baselineFile.set(
                layout.projectDirectory.file("architecture-drift.baseline")
            )
        }
        """.trimIndent(),
        )

        Files.writeString(
            projectDir.resolve("architecture.drift"),
            """
        component domain dev.shop.domain
        component infrastructure dev.shop.infrastructure
        forbid domain -> infrastructure
        """.trimIndent(),
        )

        val baselineFile =
            projectDir.resolve("architecture-drift.baseline")

        Files.writeString(
            baselineFile,
            "",
        )

        val sourceDir = projectDir.resolve(
            "src/main/kotlin/dev/shop/domain/order",
        )
        Files.createDirectories(sourceDir)

        Files.writeString(
            sourceDir.resolve("OrderService.kt"),
            """
        package dev.shop.domain.order

        class OrderService
        """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        Files.writeString(
            baselineFile,
            "\n",
        )

        val secondResult = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("architectureDriftCheck")
            .withPluginClasspath()
            .build()

        assertEquals(
            TaskOutcome.SUCCESS,
            secondResult.task(":architectureDriftCheck")?.outcome,
        )
    }
}