# ADR-0002: Use Kotlin PSI for Source Analysis

- Status: Accepted
- Date: 2026-09-16

## Context

The Kotlin source analyzer needs to discover dependencies from Kotlin
source code.

The initial implementation used regular expressions to identify packages,
imports, classes, and type usage.

This approach was sufficient to establish the first vertical slice, but
tests demonstrated important limitations:

- unused imports must not create dependencies
- references inside comments must not create dependencies
- references inside string literals must not create dependencies

Continuing to handle Kotlin syntax with regular expressions would require
reimplementing increasingly large parts of Kotlin lexical and syntactic
analysis.

Kotlin distinguishes between PSI-based syntactic analysis and semantic
symbol resolution.

## Decision

The Kotlin analyzer will use Kotlin PSI as its source-code representation.

Regular expressions will not be the long-term mechanism for parsing Kotlin
source code.

The analyzer will initially use PSI only for the syntactic information
required to extract dependencies.

Semantic symbol resolution will not be introduced until a concrete use case
requires it.

If semantic resolution becomes necessary, the Kotlin Analysis API will be
evaluated separately.

The architecture remains:

Kotlin Source
-> Kotlin PSI
-> Dependency Extraction
-> Architecture Model
-> Drift Detection

## Consequences

### Positive

- Comments and string literals can be distinguished from actual Kotlin syntax.
- The analyzer does not need to implement its own Kotlin lexer or parser.
- Source analysis remains isolated from the architecture detection core.
- Semantic analysis can be introduced incrementally when required.

### Negative

- The analyzer depends on Kotlin compiler/PSI infrastructure.
- Kotlin compiler APIs require careful version management.
- A future standalone semantic analyzer may depend on APIs that are still evolving.

## Scope

This decision applies only to `drift-analyzer-kotlin`.

`drift-core` remains independent of Kotlin PSI, compiler APIs, and source
analysis technology.