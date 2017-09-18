package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.utils.Disposable


interface Music : Disposable {

    fun play()

    fun stop()

    fun getPosition(): Float

    fun setPosition(seconds: Float)

}