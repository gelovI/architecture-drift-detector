package dev.archdrift.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

abstract class ArchitectureDriftExtension {

    abstract val architectureFile: RegularFileProperty

    abstract val sourceDirectory: DirectoryProperty
}