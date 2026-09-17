# Technical Debt

## Kotlin PSI bootstrap uses K1 API

`drift-analyzer-kotlin` currently creates its PSI environment using
`KotlinCoreEnvironment.createForProduction`.

With Kotlin 2.2.21, the compiler reports that this declaration is part of
the K1 API and that the warning will become an error in Kotlin 2.3.

The current implementation is intentionally retained because:

- the analyzer currently requires syntactic PSI only
- all analyzer tests pass
- semantic symbol resolution is not yet required
- replacing the bootstrap mechanism should be evaluated independently from
  dependency extraction behavior

Before upgrading the project to Kotlin 2.3 or later, evaluate the supported
K2/Analysis API standalone infrastructure and replace the K1 PSI bootstrap
if required.

This limitation is isolated to `drift-analyzer-kotlin`. It does not affect
`drift-core`.