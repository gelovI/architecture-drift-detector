package dev.archdrift.cli

import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val architecture = ArchitectureFileLoader()
        .load(Path.of("architecture.drift"))

    val result = CliApplication(
        architecture = architecture,
    ).run(args)

    result.output.forEach(::println)

    exitProcess(result.exitCode)
}