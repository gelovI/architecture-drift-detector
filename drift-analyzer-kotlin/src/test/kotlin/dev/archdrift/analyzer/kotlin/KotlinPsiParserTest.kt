package dev.archdrift.analyzer.kotlin

import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
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

    @Test
    fun `represents fully qualified type reference as structured syntax`() {
        val source = """
        package dev.shop.domain.order

        class OrderService(
            private val database: dev.shop.infrastructure.Database,
        )
    """.trimIndent()

        KotlinPsiParser().use { parser ->
            val file = parser.parse(source)

            val typeReferences = file
                .collectDescendantsOfType<KtTypeReference>()
                .map { it.text }

            assertEquals(
                listOf("dev.shop.infrastructure.Database"),
                typeReferences,
            )
        }
    }
}