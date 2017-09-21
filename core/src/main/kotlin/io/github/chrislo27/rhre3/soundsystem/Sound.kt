package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.utils.Disposable


interface Sound : Disposable {

    fun play(loop: Boolean = false, pitch: Float = 1f, rate: Float = 1f, volume: Float = 1f): Long

    fun setPitch(id: Long, pitch: Float)

    fun setVolume(id: Long, vol: Float)

    fun stop(id: Long)

}