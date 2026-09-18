package dev.archdrift.application

import dev.archdrift.core.Architecture
import java.nio.file.Files
import java.nio.file.Path

class ArchitectureFileLoader {

    fun load(path: Path): Architecture {
        val definition = Files.readString(path)

        return ArchitectureDefinitionParser()
            .parse(definition)
    }
}