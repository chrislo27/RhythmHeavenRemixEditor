package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.utils.Disposable


interface Sound : Disposable {

    val duration: Double

    /**
     * Plays a sound.
     * @param loop Whether or not to loop from end to end.
     * @param pitch The pitch.
     * @param rate The rate.
     * @param volume The volume.
     * @param position The position to play at. Negative positions start from the end. Interally clamped between +/- [duration].
     */
    fun play(loop: Boolean = false,
             pitch: Float = 1f, rate: Float = 1f,
             volume: Float = 1f,
             position: Double = 0.0): Long

    /**
     * Plays a sound with an specific loop section.
     * @param pitch The pitch.
     * @param rate The rate.
     * @param volume The volume.
     * @param position The position to play at. Negative positions start from the end. Interally clamped between +/- [duration].
     * @param loopParams The looping parameters.
     */
    fun playWithLoop(pitch: Float = 1f, rate: Float = 1f,
                     volume: Float = 1f,
                     position: Double = 0.0,
                     loopParams: LoopParams): Long

    fun setPitch(id: Long, pitch: Float)

    fun setVolume(id: Long, vol: Float)

    fun setRate(id: Long, rate: Float)

    fun stop(id: Long)

}