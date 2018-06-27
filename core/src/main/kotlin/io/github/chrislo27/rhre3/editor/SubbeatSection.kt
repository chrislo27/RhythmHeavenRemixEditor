package io.github.chrislo27.rhre3.editor


class SubbeatSection {

    var start: Float = 0f
    var end: Float = 0f
    var enabled: Boolean = false

    var flashAnimation: Float = 0f
    var flashAnimationSpeed: Float = 1f

    fun setFlash(time: Float) {
        if (time <= 0f)
            error("Time cannot be negative ($time)")
        flashAnimation = 1f
        flashAnimationSpeed = time
    }

}