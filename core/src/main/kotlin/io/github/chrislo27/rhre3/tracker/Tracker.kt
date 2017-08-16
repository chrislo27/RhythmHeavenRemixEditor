package io.github.chrislo27.rhre3.tracker

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme


abstract class Tracker(val beat: Float) {

    abstract fun getColor(theme: Theme): Color

    abstract fun getRenderText(): String

}