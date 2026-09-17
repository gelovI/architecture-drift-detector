package dev.archdrift.cli

data class CliResult(
    val output: List<String>,
    val exitCode: Int,
)