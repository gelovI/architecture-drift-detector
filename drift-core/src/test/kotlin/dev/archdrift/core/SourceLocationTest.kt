package dev.archdrift.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SourceLocationTest {

    @Test
    fun `stores source file and line`() {
        val location = SourceLocation(
            file = "src/main/kotlin/dev/shop/domain/order/OrderService.kt",
            line = 5,
        )

        assertEquals(
            "src/main/kotlin/dev/shop/domain/order/OrderService.kt",
            location.file,
        )
        assertEquals(5, location.line)
    }
}