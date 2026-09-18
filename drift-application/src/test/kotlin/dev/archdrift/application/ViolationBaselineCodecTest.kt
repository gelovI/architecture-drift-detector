package dev.archdrift.application

import dev.archdrift.core.ForbiddenDependencyRuleIdentity
import dev.archdrift.core.ViolationBaseline
import dev.archdrift.core.ViolationIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBaselineCodecTest {

    @Test
    fun `parses forbidden dependency baseline entry`() {
        val baseline = ViolationBaselineCodec().parse(
            """
            forbid domain -> infrastructure | dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database
            """.trimIndent(),
        )

        assertEquals(
            setOf(
                ViolationIdentity(
                    rule = ForbiddenDependencyRuleIdentity(
                        from = "domain",
                        to = "infrastructure",
                    ),
                    source = "dev.shop.domain.order.OrderService",
                    target = "dev.shop.infrastructure.Database",
                ),
            ),
            baseline.identities,
        )
    }

    @Test
    fun `parses empty baseline`() {
        val baseline = ViolationBaselineCodec().parse("")

        assertTrue(baseline.identities.isEmpty())
    }

    @Test
    fun `ignores blank lines`() {
        val baseline = ViolationBaselineCodec().parse(
            """

            forbid domain -> infrastructure | dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database

            """.trimIndent(),
        )

        assertEquals(1, baseline.identities.size)
    }

    @Test
    fun `serializes baseline deterministically`() {
        val baseline = ViolationBaseline(
            identities = setOf(
                identity(
                    source = "dev.shop.domain.order.OrderService",
                    target = "dev.shop.infrastructure.Database",
                ),
                identity(
                    source = "dev.shop.domain.customer.CustomerService",
                    target = "dev.shop.infrastructure.MessageBroker",
                ),
            ),
        )

        assertEquals(
            """
            forbid domain -> infrastructure | dev.shop.domain.customer.CustomerService -> dev.shop.infrastructure.MessageBroker
            forbid domain -> infrastructure | dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database
            """.trimIndent(),
            ViolationBaselineCodec().serialize(baseline),
        )
    }

    @Test
    fun `parse and serialize preserve baseline identities`() {
        val original = ViolationBaseline(
            identities = setOf(
                identity(
                    source = "dev.shop.domain.order.OrderService",
                    target = "dev.shop.infrastructure.Database",
                ),
                identity(
                    source = "dev.shop.domain.customer.CustomerService",
                    target = "dev.shop.infrastructure.MessageBroker",
                ),
            ),
        )

        val codec = ViolationBaselineCodec()

        val restored = codec.parse(
            codec.serialize(original),
        )

        assertEquals(
            original,
            restored,
        )
    }

    @Test
    fun `rejects malformed baseline entry`() {
        val exception = kotlin.test.assertFailsWith<IllegalArgumentException> {
            ViolationBaselineCodec().parse(
                "this is not a baseline entry",
            )
        }

        assertEquals(
            "Invalid architecture drift baseline entry: " +
                    "this is not a baseline entry",
            exception.message,
        )
    }

    @Test
    fun `parses Windows line endings`() {
        val content =
            "forbid domain -> infrastructure | " +
                    "dev.shop.domain.order.OrderService -> " +
                    "dev.shop.infrastructure.Database\r\n" +
                    "\r\n" +
                    "forbid domain -> infrastructure | " +
                    "dev.shop.domain.customer.CustomerService -> " +
                    "dev.shop.infrastructure.MessageBroker\r\n"

        val baseline = ViolationBaselineCodec().parse(content)

        assertEquals(
            2,
            baseline.identities.size,
        )
    }

    private fun identity(
        source: String,
        target: String,
    ): ViolationIdentity =
        ViolationIdentity(
            rule = ForbiddenDependencyRuleIdentity(
                from = "domain",
                to = "infrastructure",
            ),
            source = source,
            target = target,
        )
}