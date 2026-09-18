# Kotlin Analysis Capabilities

The Kotlin source analyzer discovers dependencies syntactically using Kotlin PSI.

It does not currently perform semantic symbol resolution. This keeps dependency
discovery deterministic and lightweight, but defines an explicit boundary around
which Kotlin constructs can be resolved reliably.

## Supported

The analyzer currently detects dependencies used through:

- explicit imports
- import aliases
- fully qualified type references
- class declarations
- interface declarations
- object declarations
- nested classes and objects
- companion objects
- supertypes
- annotations
- generic type arguments
- nullable types
- function parameter types
- function return types
- constructor calls
- references inside function bodies
- multiple declarations in one Kotlin file

The analyzer also:

- reports source file and line information for discovered dependencies
- preserves one dependency per source-target architecture edge
- uses the first discovered usage as the source location for repeated references
- supports CRLF and LF source files

## Not currently supported

### Wildcard imports

Example:

```kotlin
import dev.shop.infrastructure.*

class OrderService(
    private val database: Database,
)
```

Resolving OrderDatabase to the underlying Database dependency requires
declaration and symbol resolution.

This limitation also applies to type aliases declared in other source files or
packages.

Design boundary

The analyzer deliberately avoids approximating unsupported semantic cases with
string-based heuristics.

A future semantic-analysis phase should resolve symbols using Kotlin-aware
resolution rather than adding independent special cases for wildcard imports,
type aliases, and similar constructs.

Until then, architecture checks should be interpreted according to the supported
constructs documented above.


Das ist bewusst **keine ADR**. Wir treffen keine neue Architekturentscheidung; wir dokumentieren den nachgewiesenen Funktionsumfang der bestehenden Entscheidung aus ADR 0002.