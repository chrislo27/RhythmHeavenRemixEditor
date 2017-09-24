package io.github.chrislo27.rhre3.track.tracker

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


abstract class Tracker(val beat: Float) {

    abstract fun onScroll(remix: Remix, amount: Int, shift: Boolean,
                          control: Boolean)

    abstract fun getColor(theme: Theme): Color

    abstract fun getRenderText(): String

}