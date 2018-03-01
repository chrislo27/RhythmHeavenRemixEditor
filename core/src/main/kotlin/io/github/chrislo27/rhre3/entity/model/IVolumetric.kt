package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.track.Remix


interface IVolumetric {

    companion object {
        val VOLUME_RANGE = 0..300
        val DEFAULT_VOLUME = 100

        fun isRemixMutedExternally(remix: Remix): Boolean {
            return remix.cuesMuted && remix.editor.stage.tapalongStage.visible
        }
    }

    /**
     * Volume as an integral value of a percent. 100 = 100%
     */
    var volumePercent: Int

    val isVolumetric: Boolean
    val isMuted: Boolean

    val volumeRange: IntRange
        get() = VOLUME_RANGE
    val volume: Float
        get() = volumePercent / 100f

}
