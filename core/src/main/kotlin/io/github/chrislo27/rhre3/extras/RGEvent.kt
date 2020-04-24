package io.github.chrislo27.rhre3.extras

import io.github.chrislo27.rhre3.track.PlaybackCompletion


open class RGEvent(val game: RhythmGame, beat: Float) {
    
    var beat: Float = beat
    var length: Float = 0f
    var playbackCompletion = PlaybackCompletion.WAITING
    
    open fun isUpdateable(current: Float): Boolean = if (length <= 0f) current > beat else current in beat..(beat + length)
    
    open fun onStart() {
        
    }
    
    open fun whilePlaying() {

    }
    
    open fun onEnd() {

    }
}

open class RGSimpleEvent(game: RhythmGame, beat: Float, val action: () -> Unit) : RGEvent(game, beat) {

    override fun onStart() {
        action()
    }
}