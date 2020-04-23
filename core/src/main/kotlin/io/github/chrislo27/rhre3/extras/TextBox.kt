package io.github.chrislo27.rhre3.extras


data class TextBox(val text: String, var requiresInput: Boolean, var secsBeforeCanInput: Float = 0.5f)
