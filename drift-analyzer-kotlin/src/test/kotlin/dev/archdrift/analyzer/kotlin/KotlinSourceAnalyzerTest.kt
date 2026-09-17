package dev.archdrift.analyzer.kotlin

import dev.archdrift.core.Dependency
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinSourceAnalyzerTest {

    @Test
    fun `detects dependency from imported type`() {
        val source = """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService(
                private val database: Database,
            )
        """.trimIndent()

        val analyzer = KotlinSourceAnalyzer()

        val dependencies = analyzer.analyze(source)

        assertEquals(
            listOf(
                Dependency(
                    source = "dev.shop.domain.order.OrderService",
                    target = "dev.shop.infrastructure.Database",
                ),
            ),
            dependencies,
        )
    }

    @Test
    fun `does not detect dependency from unused import`() {
        val source = """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService
    """.trimIndent()

        val analyzer = KotlinSourceAnalyzer()

        val dependencies = analyzer.analyze(source)

        assertEquals(
            emptyList(),
            dependencies,
        )
    }

    @Test
    fun `does not detect dependency when imported type is only mentioned in comment`() {
        val source = """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        // Database will be integrated later.
        class OrderService
    """.trimIndent()

        val analyzer = KotlinSourceAnalyzer()

        val dependencies = analyzer.analyze(source)

        assertEquals(
            emptyList(),
            dependencies,
        )
    }

    @Test
    fun `does not detect dependency when imported type is only mentioned in string`() {
        val source = """
        package dev.shop.domain.order

        import dev.shop.infrastructure.Database

        class OrderService {
            val message = "Database will be integrated later."
        }
    """.trimIndent()

        val analyzer = KotlinSourceAnalyzer()

        val dependencies = analyzer.analyze(source)

        assertEquals(
            emptyList(),
            dependencies,
        )
    }
}