package io.github.chrislo27.rhre3.editor.view


enum class ViewType(val tag: String) {

    GAME_BOUNDARIES("gameBoundaries"),
    WAVEFORM("waveform");

    companion object {
        val VALUES = values().toList()
    }

    val localizationKey: String = "editor.view.$tag"

}