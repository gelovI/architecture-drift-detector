package dev.archdrift.core

data class ViolationBaseline(
    val identities: Set<ViolationIdentity>,
) {
    fun newViolations(
        violations: List<Violation>,
    ): List<Violation> =
        violations.filter { violation ->
            violation.identity() !in identities
        }
}