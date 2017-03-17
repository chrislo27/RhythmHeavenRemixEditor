package chrislo27.rhre.entity

import chrislo27.rhre.registry.SoundCue


data class SoundCueAction(val cue: SoundCue, val seconds: Float, val duration: Float)

interface SoundCueActionProvider {

	fun provide(): List<SoundCueAction>

}