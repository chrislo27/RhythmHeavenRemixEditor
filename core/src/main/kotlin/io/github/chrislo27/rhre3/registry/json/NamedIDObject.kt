package io.github.chrislo27.rhre3.registry.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(AbstCueObject::class),
        JsonSubTypes.Type(TempoBasedCueObject::class),
        JsonSubTypes.Type(LoopingCueObject::class),
        JsonSubTypes.Type(FillbotsFillCueObject::class),
        JsonSubTypes.Type(EquidistantObject::class),
        JsonSubTypes.Type(KeepTheBeatObject::class),
        JsonSubTypes.Type(PatternObject::class),
        JsonSubTypes.Type(RandomCueObject::class)
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
    var track: Int = 0

}

class DataObject {

    lateinit var id: String
    lateinit var name: String

    lateinit var requiresVersion: String

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var group: String? = null

    lateinit var objects: List<NamedIDObject>

}

sealed class AbstCueObject : NamedIDObject() {

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

}

@JsonTypeName("cue")
class CueObject : AbstCueObject()

@JsonTypeName("equidistant")
class EquidistantObject : NamedIDObject() {

    var distance: Float = 0f
    var stretchable: Boolean = false
    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("fillbotsFillCue")
class FillbotsFillCueObject : AbstCueObject()

@JsonTypeName("keepTheBeat")
class KeepTheBeatObject : NamedIDObject() {

    var duration: Float = 0f
    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("loopingCue")
class LoopingCueObject : AbstCueObject()

@JsonTypeName("pattern")
class PatternObject : NamedIDObject() {

    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("randomCue")
class RandomCueObject : NamedIDObject() {

    lateinit var cues: List<CuePointerObject>

}

@JsonTypeName("tempoBasedCue")
class TempoBasedCueObject : AbstCueObject() {

    var baseBpm: Float = 0f

}