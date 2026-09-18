# ADR 0006: Baseline existing architecture drift

## Status

Accepted

## Context

Architecture Drift Detector currently fails verification whenever a detected
dependency violates the intended architecture.

This behavior works well for projects that start with a clean architecture,
but it makes adoption harder for existing codebases that already contain
known architecture violations.

A project may contain legacy drift that cannot be removed immediately while
still wanting to prevent new violations from being introduced.

The detector therefore needs a way to distinguish known existing drift from
new drift.

The baseline must remain deterministic and must not depend on unstable source
location information.

## Decision

Architecture Drift Detector will support a baseline containing known
architecture violations.

A detected violation is classified as:

- **baselined** when its stable identity exists in the baseline
- **new** when its stable identity does not exist in the baseline

Only new violations fail verification.

### Violation identity

The stable identity of a violation is based on:

```text
rule + source dependency endpoint + target dependency endpoint
```

For a forbidden dependency this means the identity is derived from:

```text
forbidden component dependency
source qualified class name
target qualified class name
```

Source file paths and line numbers are not part of the baseline identity.

They are diagnostic context and may change because of unrelated source edits.

For example, moving a dependency from line 10 to line 20 must not turn a
known violation into new architecture drift.

### Stale baseline entries

A baseline entry for a violation that is no longer detected does not fail
verification.

The current version will not automatically modify the baseline and will not
require stale entries to be removed.

Baseline maintenance and stale-entry reporting may be added separately.

### Determinism

Baseline serialization and comparison must be deterministic.

The same set of violations must produce the same baseline representation
regardless of discovery order.

### Responsibility

Baseline comparison belongs to the application/domain workflow rather than
the Kotlin source analyzer.

The analyzer continues to discover source dependencies only.

Source analysis must not know whether a dependency is baselined.

Delivery adapters such as the CLI and Gradle plugin may load baseline files
and expose baseline-related configuration, but they must not implement the
comparison semantics independently.

## Consequences

Existing projects can adopt Architecture Drift Detector without first fixing
all known architecture violations.

Known violations can remain temporarily while new architecture drift still
fails verification.

Source line changes do not invalidate baseline entries.

The baseline becomes part of the project's architecture governance and should
normally be version controlled.

Stale entries may remain after violations are fixed until baseline maintenance
is performed.

The initial baseline format and delivery-adapter integration will be defined
by subsequent implementation slices.

## Deferred

This decision does not introduce:

- automatic baseline cleanup
- stale baseline warnings or failures
- wildcard or semantic Kotlin resolution
- fuzzy matching of violations
- timestamps or ownership metadata
- suppression annotations
- AI-based classification