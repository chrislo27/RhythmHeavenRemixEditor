package io.github.chrislo27.rhre3.playalong


enum class PlayalongInput(val id: String, val displayText: String) {

    BUTTON_A("button_A", PlayalongChars.FILLED_A),
    BUTTON_B("button_B", PlayalongChars.FILLED_B),
    BUTTON_DPAD("button_dpad", PlayalongChars.FILLED_DPAD),
    BUTTON_DPAD_DOWN("button_dpad_down", PlayalongChars.FILLED_DPAD_D),
    BUTTON_DPAD_RIGHT("button_dpad_right", PlayalongChars.FILLED_DPAD_R);

    companion object {
        val VALUES: List<PlayalongInput> = values().toList()
        private val INDEX_MAP: Map<PlayalongInput, Int> = VALUES.associateWith { VALUES.indexOf(it) }
        val NUMBER_RANGE: IntRange = 0 until VALUES.size

        operator fun get(id: String): PlayalongInput? = VALUES.find { it.id == id }

        fun indexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else INDEX_MAP.getOrDefault(playalongInput, -1)
    }
}