package io.github.chrislo27.rhre3.pinball


data class PinballLevel(val pins: List<PinballPin>)

data class PinballPin(val posX: Float, val posY: Float, val radius: Float = 8f)

// x - 504 to be centred