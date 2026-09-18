package dev.archdrift.core

data class ViolationIdentity(
    val rule: RuleIdentity,
    val source: String,
    val target: String,
)

sealed interface RuleIdentity

data class ForbiddenDependencyRuleIdentity(
    val from: String,
    val to: String,
) : RuleIdentity

fun Violation.identity(): ViolationIdentity =
    ViolationIdentity(
        rule = rule.identity(),
        source = dependency.source,
        target = dependency.target,
    )

private fun ArchitectureRule.identity(): RuleIdentity =
    when (this) {
        is ForbiddenDependencyRule ->
            ForbiddenDependencyRuleIdentity(
                from = from.name,
                to = to.name,
            )

        else ->
            error(
                "Unsupported architecture rule for violation identity: " +
                        this::class.qualifiedName,
            )
    }