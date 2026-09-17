package dev.archdrift.cli

fun main(args: Array<String>) {
    val result = CliApplication().run(args)

    result.output.forEach(::println)
}