# ADR 0004: Add source context to drift violations

## Status

Accepted

## Context

Version 1.0.0 can detect forbidden dependencies between architecture
components and report the qualified source and target class names.

A reported violation currently looks like this:

    dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database

This identifies the dependency that violates an architecture rule, but it does
not identify where that dependency was discovered in the source code.

This limits the usefulness of violations for developers and for future
integrations such as CI reporting, architecture visualization, remediation
guidance, and AI-assisted explanations.

Source analysis and architecture drift detection must remain separate
responsibilities. The Kotlin analyzer knows where a dependency occurs in
source code, while the core decides whether that dependency violates an
architecture rule.

## Decision

Version 2 will enrich discovered dependencies with source context.

The source context will initially contain enough information to identify the
source file and source location of a dependency.

The Kotlin source analyzer is responsible for discovering this information.

The drift core may model and preserve source context, but it must not depend
on Kotlin PSI or other Kotlin-specific implementation details.

Drift detection remains deterministic:

    Source Code
        |
        v
    Source Analysis
        |
        v
    Dependency + Source Context
        |
        v
    Architecture Rules
        |
        v
    Violation + Source Context

The CLI will use this information to produce more actionable drift reports.

## Consequences

### Positive

- Violations can point developers to the relevant source location.
- CI output can become more actionable.
- The architecture core remains independent of Kotlin PSI.
- Future explanation and remediation features can build on structured
  diagnostic information.
- Drift detection remains deterministic and testable.

### Negative

- The dependency model becomes richer.
- Existing analyzer and core tests must be adapted carefully.
- Source-location semantics must be defined precisely.
- Different source analyzers may eventually provide different kinds of
  source context.

## Out of Scope

This decision does not introduce:

- semantic Kotlin symbol resolution
- wildcard import resolution
- a Gradle plugin
- Git baselines
- architecture visualization
- AI-generated explanations
- automatic remediation
- YAML, JSON, or Kotlin DSL configuration

These capabilities may be addressed by later decisions.