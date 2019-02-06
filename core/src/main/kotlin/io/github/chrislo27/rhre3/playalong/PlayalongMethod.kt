package io.github.chrislo27.rhre3.playalong


enum class PlayalongMethod {

    PRESS,
    PRESS_AND_HOLD,
    LONG_PRESS,
    RELEASE_AND_HOLD;

//    TAP,
//    QUICK_TAP,
//    FLICK,
//    SLIDE;

    companion object {
        val VALUES = values().toList()
        private val ID_MAP: Map<String, PlayalongMethod> = VALUES.associateBy { it.name }

        operator fun get(id: String): PlayalongMethod? = ID_MAP[id]
    }

}