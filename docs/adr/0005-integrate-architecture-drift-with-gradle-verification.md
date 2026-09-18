# ADR 0005: Integrate Architecture Drift with Gradle Verification

## Status

Accepted

## Context

Architecture Drift Detector v2 can analyze Kotlin source files and directories
from the command line and produce deterministic, actionable diagnostics.

For practical use in Kotlin/JVM projects, architecture drift should be detectable
as part of the normal build and CI verification lifecycle. Requiring developers
or CI systems to invoke the command-line application separately adds integration
friction and makes architecture verification easier to omit.

The existing CLI is a delivery mechanism. Gradle integration is another delivery
mechanism and should not depend on CLI-specific argument handling, output, or
process exit codes.

## Decision

Provide Gradle integration through a dedicated `drift-gradle-plugin` module.

The plugin may depend on:

- `drift-core`
- `drift-analyzer-kotlin`

The plugin must not depend on `drift-cli`.

The initial integration will expose an architecture drift verification task for
Kotlin/JVM projects. A detected architecture violation will fail that Gradle
task, allowing the check to participate in local builds and CI pipelines.

The plugin will use the existing deterministic analyzer and drift detection
model rather than implementing a separate detection path.

The initial configuration surface will remain minimal. More advanced
configuration, source-set handling, baselines, and richer Gradle DSL design are
deferred until required by demonstrated use cases.

## Consequences

Architecture drift can become part of the build verification lifecycle rather
than requiring a separate CLI invocation.

The CLI and Gradle plugin remain independent delivery adapters over shared
analysis and detection functionality.

Gradle-specific APIs and lifecycle behavior remain isolated from the core and
Kotlin analyzer modules.

The plugin introduces Gradle integration as a new compatibility surface that
will require dedicated functional testing.

## Deferred

The following are explicitly deferred:

- semantic Kotlin symbol resolution
- wildcard import resolution
- type alias resolution
- architecture baselines
- incremental analysis
- multi-project Gradle aggregation
- advanced configuration DSL
- AI-generated explanations or remediation