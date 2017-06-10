package chrislo27.rhre.json

class GameObject {

	var gameID: String? = null
	var gameName: String? = null
	var series: String? = null

	var cues: Array<SoundObject>? = null

	var patterns: Array<PatternObject>? = null

	var usesGeneratorHelper = false
	var notRealGame: Boolean = false

	class SoundObject {

		var id: String? = null
		var fileExtension = "ogg"

		var deprecatedIDs: Array<String>? = null

		var name: String? = null

		var duration = 0.5f

		var canAlterPitch = false
		var canAlterDuration = false

		var introSound: String? = null

		var baseBpm = 0f

		var pan: Float = 0f

		var loops: Boolean? = null
	}

	class PatternObject {

		var id: String? = null
		var name: String? = null

		var deprecatedIDs: Array<String>? = null

		var isStretchable = false

		var cues: Array<CueObject>? = null

		class CueObject {

			var id: String? = null
			var beat: Float = 0.toFloat()
			var track: Int = 0

			var duration: Float? = 0f

			var semitone: Int? = 0

		}
	}

}
