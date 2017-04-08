package chrislo27.rhre.tonejs

import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx

// https://tonejs.github.io/MidiConvert/

data class ToneJsJson(var header: Header, var tracks: List<Track>?)

data class Header(var bpm: Float)

data class Track(var startTime: Double?, var notes: List<Note>?)

data class Note(var midi: Int, var time: Double, var duration: Double)

object ToneReader {

	fun read(): ToneJsJson {
		val json = Gdx.files.local("data/midi.json").readString("UTF-8")
		val obj: ToneJsJson = JsonHandler.fromJson(json)

		return obj
	}

}
