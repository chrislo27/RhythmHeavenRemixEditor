package io.github.chrislo27.rhre3.sfxdb.json

import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.*
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.*


fun Game.toJsonObject(starSubstitution: Boolean): GameObject {
    val obj = GameObject()

    obj.id = id
    obj.group = group
    obj.groupDefault = groupDefault
    obj.name = name
    obj.noDisplay = noDisplay
    obj.priority = priority
    obj.series = series.lowerCaseName

    fun String.starSubstitute(): String = if (!starSubstitution) this else ("*" + this.substringAfter(id))
    fun List<CuePointer>.starSubstituteAndMapToJson(): List<CuePointerObject> = if (!starSubstitution) mapToJsonObject() else mapToJsonObject(id)

    val objects: List<NamedIDObject> = this.objects.map { datamodel ->
        // The reverse of what happens in GameRegistry, except not compile-time checked
        when (datamodel) {
            is Cue -> {
                CueObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.baseBpm = datamodel.baseBpm
                    it.duration = datamodel.duration
                    it.fileExtension = datamodel.soundHandle.extension()
                    it.introSound = datamodel.introSound?.starSubstitute()
                    it.endingSound = datamodel.endingSound?.starSubstitute()
                    it.repitchable = datamodel.repitchable
                    it.loops = datamodel.loops
                    it.stretchable = datamodel.stretchable
                    it.responseIDs = datamodel.responseIDs.map(String::starSubstitute)
                    it.earliness = datamodel.earliness
                    it.loopStart = datamodel.loopStart
                    it.loopEnd = datamodel.loopEnd
                }
            }
            is Equidistant -> {
                EquidistantObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.stretchable = datamodel.stretchable
                    it.distance = datamodel.duration

                    it.cues = datamodel.cues.starSubstituteAndMapToJson()
                }
            }
            is KeepTheBeat -> {
                KeepTheBeatObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.defaultDuration = datamodel.duration

                    it.cues = datamodel.cues.starSubstituteAndMapToJson()
                }
            }
            is Pattern -> {
                PatternObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.stretchable = datamodel.stretchable

                    it.cues = datamodel.cues.starSubstituteAndMapToJson()
                }
            }
            is RandomCue -> {
                RandomCueObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.responseIDs = datamodel.responseIDs.map(String::starSubstitute)

                    it.cues = datamodel.cues.starSubstituteAndMapToJson()
                }
            }
            is EndRemix -> {
                EndRemixObject().also {
                    it.id = datamodel.id.starSubstitute()
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name
                }
            }
            is Subtitle -> {
                SubtitleEntityObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.subtitleType = datamodel.type.metadata
                }
            }
            is ShakeScreen -> {
                ShakeEntityObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name
                }
            }
            is TextureModel -> {
                TextureEntityObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name
                }
            }
            is TapeMeasure -> {
                TapeMeasureObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name
                }
            }
            is PlayalongModel -> {
                PlayalongEntityObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.stretchable = datamodel.stretchable
                    it.input = datamodel.playalongInput.id
                    it.method = datamodel.playalongMethod.name
                }
            }
            is MusicDistortModel -> {
                MusicDistortEntityObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name
                }
            }
            else -> error("Datamodel not defined for JSON mapping: ${datamodel::class.java.canonicalName}")
        }
    }

    obj.objects = objects

    return obj
}
