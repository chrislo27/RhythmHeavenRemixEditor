package io.github.chrislo27.rhre3.playalong


data class InputResult(val offset: Float, val missed: Boolean)

class InputResults(val inputAction: InputAction, val results: List<InputResult>) {
    val missed: Boolean = results.any { it.missed }
}