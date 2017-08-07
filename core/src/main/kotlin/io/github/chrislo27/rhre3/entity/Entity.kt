package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.track.Remix


abstract class Entity(val remix: Remix) {

    val bounds: Rectangle = Rectangle()

    abstract fun onStart()

    abstract fun whilePlaying()

    abstract fun onEnd()

}