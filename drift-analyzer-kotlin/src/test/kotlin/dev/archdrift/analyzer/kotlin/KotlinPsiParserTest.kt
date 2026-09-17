package dev.archdrift.analyzer.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinPsiParserTest {

    @Test
    fun `parses Kotlin source into structured syntax`() {
        val source = """
            package dev.shop.domain.order

            import dev.shop.infrastructure.Database

            class OrderService
        """.trimIndent()

        KotlinPsiParser().use { parser ->
            val file = parser.parse(source)

            assertEquals(
                "dev.shop.domain.order",
                file.packageFqName.asString(),
            )

            assertEquals(
                listOf("dev.shop.infrastructure.Database"),
                file.importDirectives.mapNotNull {
                    it.importedFqName?.asString()
                },
            )

            assertEquals(
                listOf("OrderService"),
                file.declarations.mapNotNull {
                    it.name
                },
            )
        }
    }
}