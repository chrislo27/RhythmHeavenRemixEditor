package io.github.chrislo27.rhre3.editor


enum class CameraBehaviour(val localizationKey: String) {

    FOLLOW_PLAYBACK("cameraBehaviour.follow"),
    PAN_OVER_INSTANT("cameraBehaviour.panOverInstant"),
    PAN_OVER_SMOOTH("cameraBehaviour.panOverSmooth");

    companion object {
        val VALUES = values().toList()
        val MAP = VALUES.associateBy { it.name }
    }

}