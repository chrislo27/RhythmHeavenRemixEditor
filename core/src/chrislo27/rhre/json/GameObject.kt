package chrislo27.rhre.json

import com.fasterxml.jackson.annotation.JsonInclude

class GameObject {

	var gameID: String? = null
	var gameName: String? = null
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	var series: String? = null

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	var priority: Int = 0

	var cues: Array<SoundObject>? = null

	var patterns: Array<PatternObject>? = null

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	var usesGeneratorHelper = false
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	var notRealGame: Boolean = false

	class SoundObject {

		var id: String? = null

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var fileExtension = "ogg"

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var deprecatedIDs: Array<String>? = null

		var name: String? = null

		var duration = 0.5f

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var canAlterPitch = false
		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var canAlterDuration = false

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var introSound: String? = null

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var baseBpm = 0f

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var pan: Float = 0f

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var loops: Boolean? = null
	}

	class PatternObject {

		var id: String? = null
		var name: String? = null

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var deprecatedIDs: Array<String>? = null

		@JsonInclude(JsonInclude.Include.NON_DEFAULT)
		var isStretchable = false

		var cues: Array<CueObject>? = null

		class CueObject {

			var id: String? = null
			var beat: Float = 0.toFloat()

			@JsonInclude(JsonInclude.Include.NON_DEFAULT)
			var track: Int = 0

			@JsonInclude(JsonInclude.Include.NON_DEFAULT)
			var duration: Float? = 0f

			@JsonInclude(JsonInclude.Include.NON_DEFAULT)
			var semitone: Int? = 0
		}
	}

}
