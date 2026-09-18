plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "architecture-drift-detector"

include(
    "drift-core",
    "drift-analyzer-kotlin",
    "drift-application",
    "drift-cli",
    "drift-gradle-plugin",
)