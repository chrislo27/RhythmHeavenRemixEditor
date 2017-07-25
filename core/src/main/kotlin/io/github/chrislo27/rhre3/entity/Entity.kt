package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.math.Rectangle


abstract class Entity {

    val bounds: Rectangle = Rectangle()

    abstract fun onStart()

    abstract fun whilePlaying()

    abstract fun onEnd()

}