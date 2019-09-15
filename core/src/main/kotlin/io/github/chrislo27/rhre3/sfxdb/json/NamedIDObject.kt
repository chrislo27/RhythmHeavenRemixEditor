package io.github.chrislo27.rhre3.sfxdb.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.entity.model.IVolumetric

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(CueObject::class),
        JsonSubTypes.Type(EquidistantObject::class),
        JsonSubTypes.Type(KeepTheBeatObject::class),
        JsonSubTypes.Type(PatternObject::class),
        JsonSubTypes.Type(RandomCueObject::class),
        JsonSubTypes.Type(EndRemixObject::class),
        JsonSubTypes.Type(SubtitleEntityObject::class),
        JsonSubTypes.Type(ShakeEntityObject::class),
        JsonSubTypes.Type(TextureEntityObject::class),
        JsonSubTypes.Type(TapeMeasureObject::class),
        JsonSubTypes.Type(PlayalongEntityObject::class),
        JsonSubTypes.Type(MusicDistortEntityObject::class)
             )
sealed class NamedIDObject {

    lateinit var id: String
    lateinit var deprecatedIDs: List<String>
    lateinit var name: String

}

class CuePointerObject {

    lateinit var id: String
    var beat: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var duration: Float = 0f
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var semitone: Int = 0
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var volume: Int = IVolumetric.DEFAULT_VOLUME
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var track: Int = 0

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var metadata: Map<String, Any?>? = null

}

class GameObject {

    lateinit var id: String
    lateinit var name: String

//    lateinit var requiresVersion: String

    var series: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var language: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var group: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var groupDefault: Boolean = false

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var priority: Int = 0

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var searchHints: List<String>? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var noDisplay: Boolean = false

    lateinit var objects: List<NamedIDObject>

}

@JsonTypeName("cue")
class CueObject : NamedIDObject() {

    var duration: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var stretchable: Boolean = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var repitchable: Boolean = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var fileExtension: String = "ogg"

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var introSound: String? = null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var endingSound: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var responseIDs: List<String> = listOf()

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var baseBpm: Float = 0f
    @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = UseTimeStretchingValueFilter::class)
    var useTimeStretching: Boolean = true

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var loops: Boolean = false

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var earliness: Float = 0f
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var loopStart: Float = 0f
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var loopEnd: Float = -1f
    
    @Suppress("EqualsOrHashCode")
    class UseTimeStretchingValueFilter {
        override fun equals(other: Any?): Boolean {
            if (other is Boolean) {
                return other
            }
            return super.equals(other)
        }
    }

}

@JsonTypeName("equidistant")
class EquidistantObject : NamedIDObject() {

    var distance: Float = 0f
    var stretchable: Boolean = false
    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("keepTheBeat")
class KeepTheBeatObject : NamedIDObject() {

    var defaultDuration: Float = 0f
    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("pattern")
class PatternObject : NamedIDObject() {

    lateinit var cues: List<CuePointerObject>

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var stretchable: Boolean = false

}

@JsonTypeName("randomCue")
class RandomCueObject : NamedIDObject() {

    lateinit var cues: List<CuePointerObject>

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var responseIDs: List<String> = listOf()

}

@JsonTypeName("endEntity")
class EndRemixObject : NamedIDObject()

@JsonTypeName("subtitleEntity")
class SubtitleEntityObject : NamedIDObject() {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var subtitleType: String? = null

}

@JsonTypeName("shakeEntity")
class ShakeEntityObject : NamedIDObject()

@JsonTypeName("textureEntity")
class TextureEntityObject : NamedIDObject()

@JsonTypeName("tapeMeasure")
class TapeMeasureObject : NamedIDObject()

@JsonTypeName("playalongEntity")
class PlayalongEntityObject : NamedIDObject() {
    var stretchable: Boolean = false
    var method: String? = null
    var input: String? = null
}

@JsonTypeName("musicDistortEntity")
class MusicDistortEntityObject : NamedIDObject()
