package dev.archdrift.application

import dev.archdrift.core.ViolationBaseline
import java.nio.file.Files
import java.nio.file.Path

class ViolationBaselineFileLoader(
    private val codec: ViolationBaselineCodec = ViolationBaselineCodec(),
) {

    fun load(path: Path): ViolationBaseline =
        codec.parse(
            Files.readString(path),
        )
}