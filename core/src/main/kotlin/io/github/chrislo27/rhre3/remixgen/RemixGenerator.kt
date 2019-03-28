package io.github.chrislo27.rhre3.remixgen

import io.github.chrislo27.rhre3.track.Remix
import java.util.*


class RemixGenerator(val remix: Remix, val settings: RemixGeneratorSettings) {

    private val random: Random = Random(settings.seed)

}