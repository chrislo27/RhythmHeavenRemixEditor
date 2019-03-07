package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix


interface IVolumetric {

    companion object {
        val VOLUME_RANGE = 0..300
        val DEFAULT_VOLUME = 100

        private val volumeTextCache: MutableMap<Int, String> = mutableMapOf()

        fun isRemixMutedExternally(remix: Remix): Boolean {
            return remix.cuesMuted && remix.main.screen is EditorScreen
        }

        // Caching
        fun getVolumeText(volume: Int): String {
            return volumeTextCache.getOrPut(volume) {
                Editor.VOLUME_CHAR + volume + "%"
            }
        }
    }

    /**
     * Volume as an integral value of a percent. 100 = 100%
     */
    var volumePercent: Int
    /**
     * A coefficient to be applied for [volume]. This is used for parents who affect their children's volumes multiplicatively.
     */
    var volumeCoefficient: Float

    val isVolumetric: Boolean
    val isMuted: Boolean

    val volumeRange: IntRange
        get() = VOLUME_RANGE
    val volume: Float
        get() = if (isMuted) 0f else (volumePercent / 100f * volumeCoefficient)

    val persistVolumeData: Boolean
        get() = true

}
