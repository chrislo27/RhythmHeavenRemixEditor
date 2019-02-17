package io.github.chrislo27.rhre3.playalong


enum class PlayalongMethod(val instantaneous: Boolean) {

    PRESS(true),
    PRESS_AND_HOLD(false),
    LONG_PRESS(false),
    RELEASE_AND_HOLD(false),
    RELEASE(true); // RELEASE is for Quick Tap

    companion object {
        val VALUES = values().toList()
        private val ID_MAP: Map<String, PlayalongMethod> = VALUES.associateBy { it.name }

        operator fun get(id: String): PlayalongMethod? = ID_MAP[id]
    }

}