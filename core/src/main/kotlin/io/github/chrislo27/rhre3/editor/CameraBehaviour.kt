package io.github.chrislo27.rhre3.editor


enum class CameraBehaviour(val localizationKey: String) {

    FOLLOW_PLAYBACK("cameraBehaviour.follow"),
    ROLL_OVER_INSTANT("cameraBehaviour.rollOverInstant"),
    ROLL_OVER_SMOOTH("cameraBehaviour.rollOverSmooth");

    companion object {
        val VALUES = values().toList()
        val MAP = VALUES.associateBy { it.name }
    }

}