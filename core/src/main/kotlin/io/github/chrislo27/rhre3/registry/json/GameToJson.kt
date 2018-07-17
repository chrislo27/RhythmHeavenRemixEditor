package io.github.chrislo27.rhre3.registry.json

import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.ShakeScreen
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.TextureModel


fun Game.toJsonObject(): DataObject {
    val obj = DataObject()

    obj.id = id
    obj.group = group
    obj.groupDefault = groupDefault
    obj.name = name
    obj.noDisplay = noDisplay
    obj.priority = priority
    obj.series = series.lowerCaseName

    val objects: List<NamedIDObject> = this.objects.map { datamodel ->
        // The reverse of what happens in GameRegistry, except not compile-time checked
        when (datamodel) {
            is Cue -> {
                CueObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.baseBpm = datamodel.baseBpm
                    it.duration = datamodel.duration
                    it.fileExtension = datamodel.soundHandle.extension()
                    it.introSound = datamodel.introSound
                    it.endingSound = datamodel.endingSound
                    it.repitchable = datamodel.repitchable
                    it.loops = datamodel.loops
                    it.stretchable = datamodel.stretchable
                }
            }
            is Equidistant -> {
                EquidistantObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.stretchable = datamodel.stretchable
                    it.distance = datamodel.duration

                    it.cues = datamodel.cues.mapToJsonObject()
                }
            }
            is KeepTheBeat -> {
                KeepTheBeatObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.defaultDuration = datamodel.duration

                    it.cues = datamodel.cues.mapToJsonObject()
                }
            }
            is Pattern -> {
                PatternObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.stretchable = datamodel.stretchable

                    it.cues = datamodel.cues.mapToJsonObject()
                }
            }
            is RandomCue -> {
                RandomCueObject().also {
                    it.id = datamodel.id
                    it.deprecatedIDs = datamodel.deprecatedIDs
                    it.name = datamodel.name

                    it.responseIDs = datamodel.responseIDs

                    it.cues = datamodel.cues.mapToJsonObject()
                }
            }
            is EndRemix -> {
                EndRemixObject().also {
                    it.id = datamodel.id
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
            else -> error("Datamodel not defined for JSON mapping")
        }
    }

    obj.objects = objects

    return obj
}
