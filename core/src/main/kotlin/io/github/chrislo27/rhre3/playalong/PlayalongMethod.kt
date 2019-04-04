package io.github.chrislo27.rhre3.playalong


enum class PlayalongMethod(val instantaneous: Boolean, val isRelease: Boolean) {

    PRESS(true, false),
    PRESS_AND_HOLD(false, false),
    LONG_PRESS(false, false),
    RELEASE_AND_HOLD(false, true),
    RELEASE(true, true); // RELEASE is for Quick Tap

    companion object {
        val VALUES = values().toList()
        private val ID_MAP: Map<String, PlayalongMethod> = VALUES.associateBy { it.name }

        operator fun get(id: String): PlayalongMethod? = ID_MAP[id]
    }

}