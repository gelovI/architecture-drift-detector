package dev.archdrift.application

import dev.archdrift.core.ForbiddenDependencyRuleIdentity
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationBaselineFileLoaderTest {

    @Test
    fun `loads baseline from file`() {
        val baselineFile = createTempFile(
            prefix = "architecture-drift-",
            suffix = ".baseline",
        )

        baselineFile.writeText(
            """
            forbid domain -> infrastructure | dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database
            """.trimIndent(),
        )

        val baseline = ViolationBaselineFileLoader().load(baselineFile)

        assertEquals(
            1,
            baseline.identities.size,
        )

        val identity = baseline.identities.single()

        assertEquals(
            ForbiddenDependencyRuleIdentity(
                from = "domain",
                to = "infrastructure",
            ),
            identity.rule,
        )

        assertEquals(
            "dev.shop.domain.order.OrderService",
            identity.source,
        )

        assertEquals(
            "dev.shop.infrastructure.Database",
            identity.target,
        )
    }
}