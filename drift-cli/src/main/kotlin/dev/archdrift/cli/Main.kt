package dev.archdrift.cli

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val result = CliApplication().run(args)

    result.output.forEach(::println)

    exitProcess(result.exitCode)
}