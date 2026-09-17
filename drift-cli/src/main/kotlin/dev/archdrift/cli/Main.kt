package dev.archdrift.cli

fun main(args: Array<String>) {
    CliApplication()
        .run(args)
        .forEach(::println)
}