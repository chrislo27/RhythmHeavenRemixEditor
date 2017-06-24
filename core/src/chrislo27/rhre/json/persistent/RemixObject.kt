package chrislo27.rhre.json.persistent

import chrislo27.rhre.track.MusicData
import chrislo27.rhre.version.RHRE2Version
import com.badlogic.gdx.files.FileHandle

class RemixObject {

	@Transient var fileHandle: FileHandle? = null
	@Transient var musicData: MusicData? = null

	var musicAssociation: String? = null

	var version: String? = null
	val versionNumber by lazy {
		RHRE2Version.fromString(version ?: throw IllegalStateException("Attempt to compute version number when version is null")).numericalValue
	}
	var entities: MutableList<EntityObject>? = null

	var playbackStart: Float = 0f
	var musicVolume = 1f
	var musicStartTime: Float = 0f
	var bpmChanges: MutableList<BpmTrackerObject>? = null

	var metadata = MetadataObject()

	class EntityObject {

		var id: String? = null
		var beat: Float = 0f
		var level: Int = 0

		// optionals
		var isPattern: Boolean = false
		var width: Float = 0f
		var semitone: Int = 0

		var stopAlways: Boolean = false
		var volume: Float = 1f

	}

	class BpmTrackerObject {

		var beat: Float = 0f
		var tempo: Float = 0f

	}

	class MetadataObject {

		var author: String? = null
		var description: String? = null
		var gamesUsed: String? = null

	}

}
