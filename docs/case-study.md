# Architecture Drift Detector - Engineering Case Study

## Problem

Software architecture often starts with clear boundaries: domain code should remain independent from infrastructure, dependencies should point in specific directions, and architectural layers should have explicit responsibilities.

As a codebase evolves, those boundaries can gradually be violated by implementation dependencies. If architecture rules exist only as documentation or team conventions, these violations are difficult to enforce consistently in local development and CI.

Architecture Drift Detector was built to make a small but important subset of those rules executable.

The initial rule is deliberately simple:

```text
domain -X-> infrastructure
```

If Kotlin source code introduces a dependency from a class in the domain component to a class in the infrastructure component, the detector reports architecture drift.

## Goal

The goal was not to build a general-purpose architecture platform immediately.

The project instead focused on a deterministic vertical slice that could evolve into a usable developer tool:

- analyze Kotlin/JVM source code
- discover dependencies from source
- compare those dependencies with explicit architecture rules
- produce actionable diagnostics
- integrate verification into Gradle and CI
- support incremental adoption in codebases with existing violations

A core design constraint was that architecture detection must not depend on an LLM. Given the same source code and architecture definition, the detector should produce the same result.

## Architecture

The project is organized as a Gradle multi-module build:

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

Each module has a distinct responsibility.

### `drift-core`

Contains the architecture model, dependency model, architecture rules, violations, stable violation identities, baseline classification, and deterministic drift detection.

The core does not know how Kotlin source code is parsed or how results are presented to users.

### `drift-analyzer-kotlin`

Uses Kotlin PSI to analyze Kotlin source code and discover dependencies.

Its responsibility ends at dependency discovery. It does not decide whether a dependency violates the intended architecture.

### `drift-application`

Orchestrates architecture loading, source analysis, directory traversal, drift detection, and baseline-aware workflows.

### `drift-cli`

Provides command-line access to detection and baseline workflows.

### `drift-gradle-plugin`

Integrates architecture verification with Gradle's `check` lifecycle and exposes Gradle-native configuration, inputs, and outputs.

This separation was intentional: source analysis and architecture policy evolve for different reasons and should remain independently testable.

## Key Engineering Decisions

### Kotlin PSI instead of text matching

An early architectural decision was to use Kotlin PSI for source analysis rather than regular expressions or ad-hoc text parsing.

The analyzer can therefore reason about Kotlin syntax such as imports, type references, annotations, supertypes, constructor calls, function signatures, nested declarations, and fully qualified references.

The current implementation remains syntactic. It deliberately does not pretend to provide semantic symbol resolution where none exists.

Cases such as wildcard imports and type aliases are therefore documented limitations rather than being approximated with unreliable heuristics.

### Detection belongs to the core

The analyzer discovers facts:

```text
source -> target
```

The core evaluates those facts against architecture rules.

For example:

```text
dev.shop.domain.order.OrderService
    ->
dev.shop.infrastructure.Database
```

becomes architecture drift only when an architecture rule forbids the corresponding component dependency.

Keeping this decision out of the analyzer prevents Kotlin-specific parsing concerns from becoming coupled to architecture policy.

### Actionable diagnostics

A detector that only reports two class names provides limited feedback during development.

The dependency model was therefore extended with source context so violations can be reported in a form such as:

```text
src/main/kotlin/dev/shop/domain/order/OrderService.kt:6: forbidden dependency domain -> infrastructure: dev.shop.domain.order.OrderService -> dev.shop.infrastructure.Database
```

This makes the output usable in local development and CI without changing the deterministic detection model.

### Gradle-native verification

The detector was integrated as a Gradle plugin rather than requiring CI scripts to reproduce CLI orchestration.

The plugin registers an `architectureDriftCheck` task and connects it to Gradle's normal `check` lifecycle.

Architecture configuration, source directories, and an optional baseline are modeled as task inputs. The task therefore participates in Gradle validation and up-to-date checking instead of implementing those mechanisms independently.

### Stable baseline identity

Existing projects may already contain architecture violations that cannot be removed immediately.

To support incremental adoption, the detector can store known violations in a baseline.

A violation is identified by:

```text
rule + source + target
```

Source file and line number are intentionally excluded.

This distinction separates violation identity from diagnostic location. Moving an existing violation to another line does not make it a new architecture violation.

Baseline comparison has simple deterministic semantics:

```text
detected + in baseline      -> existing drift
detected + not in baseline  -> new drift
baseline + not detected     -> stale entry, ignored
```

This allows CI to tolerate explicitly recorded legacy drift while rejecting newly introduced drift.

## Testing Strategy

The implementation was developed incrementally with automated tests at the relevant architectural boundaries.

The test suite includes:

- core unit tests for rules, violations, identities, and baseline classification
- Kotlin analyzer characterization tests
- application workflow tests
- CLI behavior tests
- Gradle TestKit functional tests
- a separate Gradle consumer build for end-to-end verification

The analyzer tests are particularly important because PSI-based source analysis has many syntactic edge cases.

The Gradle functional tests verify behavior such as task registration, lifecycle integration, configuration validation, actionable failures, baseline handling, and up-to-date behavior.

## Incremental Evolution

The project was developed through four explicit releases.

### v1.0.0 - Detection

The first vertical slice established the fundamental model:

```text
source -> target
```

Kotlin dependencies were discovered and evaluated against a forbidden dependency rule.

### v2.0.0 - Actionable diagnostics

Detection was extended with source context and deterministic diagnostic output:

```text
file:line + violated rule + source -> target
```

The Kotlin analyzer's supported syntax and limitations were characterized and documented.

### v3.0.0 - Gradle and CI integration

Architecture verification became part of the normal Gradle lifecycle.

The plugin introduced typed configuration, task inputs and outputs, Gradle validation, up-to-date behavior, and a separate consumer example.

### v4.0.0 - Architecture baselines

Stable violation identity and baseline comparison made incremental adoption possible.

Existing drift can be recorded while CI continues to reject new architecture violations.

Both the CLI and Gradle integration support this workflow.

## Trade-offs and Current Limitations

The current Kotlin analyzer is PSI-based and syntactic rather than a full semantic compiler analysis.

This keeps the implementation focused and deterministic but means that some Kotlin constructs cannot yet be resolved correctly without additional semantic information.

Known limitations include:

- wildcard import resolution
- type-alias resolution
- full semantic symbol resolution
- advanced multi-project Gradle configuration
- richer architecture-rule DSLs

These limitations are documented explicitly rather than hidden behind heuristics.

The project also currently focuses on forbidden component dependencies. The architecture is designed so additional rule types can be introduced later without moving source-analysis responsibilities into the detection core.

## Result

Architecture Drift Detector evolved from a minimal dependency-rule experiment into a usable Kotlin/JVM verification tool.

The current system can:

- analyze Kotlin source code using PSI
- detect forbidden architectural dependencies deterministically
- report violations with actionable source context
- fail Gradle and CI verification
- participate in Gradle up-to-date checking
- baseline existing architecture drift
- reject only newly introduced drift when a baseline is configured

The result is intentionally narrower than a full architecture-analysis platform, but the implemented path is end-to-end and executable rather than conceptual.

The central engineering principle remained consistent throughout the project:

> Source analysis discovers dependencies. Architecture policy decides whether those dependencies are allowed.

That separation provides the foundation for extending source analysis, architecture rules, delivery mechanisms, or future explanation capabilities without making deterministic architecture enforcement depend on them.