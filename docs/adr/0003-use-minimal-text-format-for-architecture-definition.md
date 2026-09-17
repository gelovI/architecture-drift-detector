# ADR 0003: Use a minimal text format for architecture definitions

## Status

Accepted

## Context

The architecture drift detector needs an explicit representation of the intended
architecture.

The core already models architectures using components and architecture rules,
but the CLI currently creates the concrete architecture in its composition root.

Users need a way to define the intended architecture outside the application
code.

Possible approaches include:

- a custom minimal text format
- JSON
- YAML
- a Kotlin DSL

For the first version, introducing a serialization framework or designing a
full Kotlin DSL would add complexity before the required architecture
definition semantics are established.

## Decision

The first external architecture definition format will be a minimal,
line-oriented text format.

Example:

```text
component domain dev.shop.domain
component infrastructure dev.shop.infrastructure
forbid domain -> infrastructure