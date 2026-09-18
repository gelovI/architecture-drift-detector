# Architecture Drift Detector

Architecture Drift Detector is a deterministic Kotlin/JVM developer tool for detecting dependencies that violate an intended software architecture.

It compares dependencies discovered in Kotlin source code with architecture rules defined in a small text configuration and reports violations with source file and line information.

The detector can be used from the command line or integrated into Gradle verification so architecture drift can fail a normal `check` build in CI.

## Why

Software architecture can gradually diverge from its intended design as dependencies are introduced during development.

For example, an application may define a rule that the domain layer must not depend on infrastructure:

```text
domain -X-> infrastructure
```

but the implementation may contain:

```kotlin
package dev.shop.domain.order

import dev.shop.infrastructure.Database

class OrderService(
    private val database: Database,
)
```

Architecture Drift Detector makes that difference executable and measurable.

## Example diagnostic

A forbidden dependency is reported with its source location, violated rule, and dependency:

```text
Architecture drift detected:
src/main/kotlin/dev/shop/domain/order/OrderService.kt:6: forbidden dependency domain -> infrastructure: dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database
```

The result is deterministic and does not require an LLM.

## Architecture definition

Architecture rules are defined in `architecture.drift`.

Example:

```text
component domain dev.shop.domain
component infrastructure dev.shop.infrastructure

forbid domain -> infrastructure
```

This declares two architectural components based on package prefixes and forbids dependencies from `domain` to `infrastructure`.

The configuration format is intentionally minimal in the current version.

## Gradle integration

The Gradle plugin registers:

```text
architectureDriftCheck
```

and integrates it with Gradle's normal `check` lifecycle.

A project using the plugin can therefore run:

```bash
./gradlew check
```

On Windows:

```powershell
.\gradlew.bat check
```

A detected architecture violation fails the Gradle build.

### Configuration

By default, the plugin uses:

```text
architecture.drift
src/main/kotlin
```

Projects with a different layout can configure both paths:

```kotlin
architectureDrift {
    architectureFile.set(
        layout.projectDirectory.file("config/architecture.drift")
    )

    sourceDirectory.set(
        layout.projectDirectory.dir("src/main/kotlin")
    )
}
```

The task declares its architecture definition and source directory as Gradle inputs and supports Gradle up-to-date checking.

A local consumer example is available in:

```text
examples/gradle-plugin
```

The example resolves the unpublished plugin through a Gradle composite build.

## CLI

The detector can also be executed through the CLI module.

Example:

```powershell
.\gradlew.bat :drift-cli:run --args="architecture.drift examples\clean"
```

A clean project exits successfully and reports:

```text
No architecture drift detected.
```

A project containing a forbidden dependency reports the violation and exits with a non-zero status.

Example:

```powershell
.\gradlew.bat :drift-cli:run --args="architecture.drift examples\broken"
```

## Modules

The project is split into explicit architectural responsibilities:

```text
drift-core
    ^
    |
drift-analyzer-kotlin
    ^
    |
drift-application
    ^             ^
    |             |
drift-cli    drift-gradle-plugin
```

### `drift-core`

Contains the architecture model, dependency model, rules, violations, and deterministic drift detection.

It does not know how source code is analyzed or how results are delivered to users.

### `drift-analyzer-kotlin`

Analyzes Kotlin source code using Kotlin PSI and discovers dependencies.

Source analysis is deliberately separated from architecture-rule evaluation.

### `drift-application`

Orchestrates architecture loading, Kotlin source analysis, directory traversal, and drift detection for delivery adapters.

### `drift-cli`

Provides command-line delivery and diagnostic formatting.

### `drift-gradle-plugin`

Provides Gradle integration through a typed extension and `architectureDriftCheck` verification task.

## Requirements

- Java 17 or newer
- Gradle 8.14 through the included Gradle Wrapper

The project is implemented with Kotlin/JVM 2.2.21.

## Build and test

Run the complete test suite:

```powershell
.\gradlew.bat clean test
```

On Unix-like systems:

```bash
./gradlew clean test
```

The project includes unit tests, analyzer characterization tests, CLI tests, and Gradle TestKit functional tests.

## Kotlin analysis capabilities

The Kotlin analyzer is currently syntactic and PSI-based. It does not perform full semantic symbol resolution.

Supported scenarios and known limitations are documented in:

- `docs/kotlin-analysis-capabilities.md`

In particular, semantic cases such as wildcard imports and type aliases are intentionally not approximated with heuristics.

## Design decisions

Architectural decisions are recorded as ADRs:

1. `docs/adr/0001-separate-detection-from-source-analysis.md`
2. `docs/adr/0002-use-kotlin-psi-for-source-analysis.md`
3. `docs/adr/0003-use-minimal-text-format-for-architecture-definition.md`
4. `docs/adr/0004-add-source-context-to-drift-violations.md`
5. `docs/adr/0005-integrate-architecture-drift-with-gradle-verification.md`

Known technical debt is documented in:

- `docs/technical-debt.md`

## Current scope

The current version focuses on deterministic detection of forbidden dependencies in Kotlin/JVM projects.

Deliberately deferred areas include:

- semantic Kotlin symbol resolution
- wildcard import resolution
- type-alias resolution
- architecture baselines
- advanced multi-project Gradle configuration
- richer architecture-rule DSLs
- AI-generated explanations or remediation

These are kept outside the core detection path so the current detector remains deterministic and testable.

## Releases

- `v1.0.0` - first end-to-end architecture drift detection
- `v2.0.0` - actionable deterministic diagnostics with source context
- `v3.0.0` - Gradle/CI integration