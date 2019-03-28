package io.github.chrislo27.rhre3.remixgen

import io.github.chrislo27.rhre3.track.Remix
import java.util.*


class RemixGenerator(val remix: Remix, val settings: RemixGeneratorSettings) {

    val rootBeat: Int = remix.timeSignatures.map.values.firstOrNull { it.divisions % 2 == 0 }?.beat ?: 0
    val canGenerate: Boolean = remix.duration < Float.POSITIVE_INFINITY && remix.remixGeneratorSettings == null
    private val random: Random = Random(settings.seed)

    /**
     * Generates the remix. Calling this function multiple times leads to undefined behaviour.
     */
    fun generate() {
        if (!canGenerate) return

        remix.remixGeneratorSettings = settings.copy()
    }

}