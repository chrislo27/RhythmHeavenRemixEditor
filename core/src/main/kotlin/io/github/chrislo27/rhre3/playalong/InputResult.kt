package io.github.chrislo27.rhre3.playalong

import kotlin.math.absoluteValue


data class InputResult(val offset: Float, val timing: InputTiming = InputTiming.getFromOffset(offset))

class InputResults(val inputAction: InputAction, val results: List<InputResult>) {
    val missed: Boolean = results.any { it.timing == InputTiming.MISS }
}

enum class InputTiming(val scoreWeight: Float) {
    ACE(1.0f), GOOD(0.85f), BARELY(0.6f), MISS(0f);

    companion object {
        fun getFromOffset(offset: Float): InputTiming {
            val o = offset.absoluteValue
            return when {
                o > Playalong.MAX_OFFSET_SEC -> MISS
                o <= Playalong.ACE_OFFSET -> ACE
                o <= Playalong.GOOD_OFFSET -> GOOD
                o <= Playalong.BARELY_OFFSET -> BARELY
                else -> MISS
            }
        }
    }
}