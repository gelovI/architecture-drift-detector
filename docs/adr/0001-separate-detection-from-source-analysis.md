# ADR-0001: Separate Architecture Detection from Source-Code Analysis

- Status: Accepted
- Date: 2026-09-16

## Context

The Architecture Drift Detector needs to compare the intended architecture
of a software system with the dependencies that actually exist in its code.

Discovering dependencies in source code and deciding whether those
dependencies violate architectural rules are different responsibilities.

If architecture detection directly depends on Kotlin source-code parsing,
the core domain would become coupled to a particular source language and
analysis technology.

## Decision

Architecture drift detection will be separated from source-code analysis.

The source-code analyzer is responsible for discovering dependencies and
transforming them into the architecture domain model.

The drift detection core operates only on architecture concepts such as:

- components
- dependencies
- architecture rules
- violations

The detection core does not know how dependencies were discovered.

Conceptually:

Source Code
-> Source Analysis
-> Architecture Model
-> Drift Detection
-> Violations

## Consequences

### Positive

- The detection core remains independent of Kotlin parsing technology.
- Different analyzers can be added later.
- Domain logic can be tested without parsing source files.
- Architecture rules remain separate from technical source analysis.

### Negative

- A translation boundary between source analysis and the domain model is required.
- The project contains more explicit architectural boundaries than a single-module implementation.

## Scope

This decision defines the architectural direction of the project.

It does not yet select a Kotlin parsing library or define the complete
Gradle module structure.