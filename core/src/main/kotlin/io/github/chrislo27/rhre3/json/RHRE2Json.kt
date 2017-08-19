package io.github.chrislo27.rhre3.json

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.version.Version

class RemixObject {

    @Transient var fileHandle: FileHandle? = null
    @Transient var musicData: JsonMusicData? = null

    var musicAssociation: String? = null

    var version: String? = null
    val versionNumber: Int?
        get() {
            return Version.fromString(version ?: return null).numericalValue
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

    data class JsonMusicData(val music: Music, val file: FileHandle, val originalFileName: String) : Disposable {

        override fun dispose() {
            music.dispose()
        }
    }

}
