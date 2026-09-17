package dev.archdrift.cli

import dev.archdrift.core.Architecture
import dev.archdrift.core.Component
import dev.archdrift.core.ForbiddenDependencyRule

fun testArchitecture(): Architecture {
    val domain = Component(
        name = "domain",
        packagePrefix = "dev.shop.domain",
    )

    val infrastructure = Component(
        name = "infrastructure",
        packagePrefix = "dev.shop.infrastructure",
    )

    return Architecture(
        components = listOf(
            domain,
            infrastructure,
        ),
        rules = listOf(
            ForbiddenDependencyRule(
                from = domain,
                to = infrastructure,
            ),
        ),
    )
}