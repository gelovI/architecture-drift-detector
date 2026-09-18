package dev.archdrift.application

import dev.archdrift.core.ForbiddenDependencyRuleIdentity
import dev.archdrift.core.ViolationBaseline
import dev.archdrift.core.ViolationIdentity

class ViolationBaselineCodec {

    fun parse(content: String): ViolationBaseline =
        ViolationBaseline(
            identities = content
                .lineSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .map(::parseLine)
                .toSet(),
        )

    fun serialize(
        baseline: ViolationBaseline,
    ): String =
        baseline.identities
            .map(::serializeIdentity)
            .sorted()
            .joinToString("\n")

    private fun parseLine(
        line: String,
    ): ViolationIdentity {
        val match = BASELINE_ENTRY.matchEntire(line)
            ?: throw IllegalArgumentException(
                "Invalid architecture drift baseline entry: $line",
            )

        return ViolationIdentity(
            rule = ForbiddenDependencyRuleIdentity(
                from = match.groupValues[1],
                to = match.groupValues[2],
            ),
            source = match.groupValues[3],
            target = match.groupValues[4],
        )
    }

    private fun serializeIdentity(
        identity: ViolationIdentity,
    ): String =
        when (val rule = identity.rule) {
            is ForbiddenDependencyRuleIdentity ->
                "forbid ${rule.from} -> ${rule.to} | " +
                        "${identity.source} -> ${identity.target}"
        }

    private companion object {
        val BASELINE_ENTRY = Regex(
            """forbid\s+(\S+)\s+->\s+(\S+)\s+\|\s+(\S+)\s+->\s+(\S+)""",
        )
    }
}