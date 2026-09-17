package dev.archdrift.cli

import java.nio.file.Path

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: architecture-drift-detector <kotlin-file>")
        return
    }

    val sourceFile = Path.of(args[0])

    val violations = DriftFileAnalyzer()
        .detect(sourceFile)

    if (violations.isEmpty()) {
        println("No architecture drift detected.")
        return
    }

    println("Architecture drift detected:")

    violations.forEach(::println)
}