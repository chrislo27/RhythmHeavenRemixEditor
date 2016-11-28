package chrislo27.rhre.track

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.registry.GameRegistry
import com.badlogic.gdx.audio.Music

class Remix {

	val entities: MutableList<Entity> = mutableListOf()
	val selection: List<Entity> = mutableListOf()
	val tempoChanges: TempoChanges = TempoChanges(120f)

	private var playingState = PlayingState.STOPPED
	var music: Music? = null

	init {
		entities.add(SoundEntity(this, GameRegistry.instance().get("tapTrial").getCue("ook"), 0f, 0))
		entities.add(SoundEntity(this, GameRegistry.instance().get("tapTrial").getCue("tap"), 1f, 0))
	}

	fun setPlayingState(ps: PlayingState) {
		playingState = ps

		when (ps) {
			PlayingState.PLAYING -> {
			}
			PlayingState.PAUSED -> {
			}
			PlayingState.STOPPED -> {
			}
		}
	}

	fun update(delta: Float) {
		if (playingState != PlayingState.PLAYING)
			return@update
	}

}

enum class PlayingState {
	PLAYING, PAUSED, STOPPED;
}

