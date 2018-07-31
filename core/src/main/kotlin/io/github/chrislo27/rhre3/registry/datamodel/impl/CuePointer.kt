package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.registry.json.CuePointerObject

fun CuePointerObject.toDatamodel(): CuePointer = CuePointer(this)
fun CuePointer.toJsonObject(): CuePointerObject {
    return CuePointerObject().also {
        it.id = this.id
        it.beat = this.beat
        it.duration = this.backingDuration
        it.semitone = this.semitone
        it.volume = this.volume
        it.track = this.track
        it.metadata = this.metadata
    }
}

fun List<CuePointerObject>.mapToDatamodel(): List<CuePointer> = this.map(::CuePointer)
fun List<CuePointerObject>.mapToDatamodel(starSubstitution: String): List<CuePointer> = this.map { CuePointer(it, starSubstitution) }

fun List<CuePointer>.mapToJsonObject(): List<CuePointerObject> = this.map(CuePointer::toJsonObject)

class CuePointer {

    val id: String
    val beat: Float

    val backingDuration: Float
    val track: Int

    val semitone: Int
    val volume: Int

    val duration: Float
        get() =
            if (backingDuration <= 0f) {
                (GameRegistry.data.objectMap[id] as? DurationModel)?.duration ?: error("$id is not a DurationModel")
            } else {
                backingDuration
            }

    val metadata: Map<String, Any?>

    constructor(obj: CuePointerObject, starSubstitution: String?) {
        id = if (starSubstitution == null) obj.id else obj.id.replace("*", starSubstitution)
        beat = obj.beat
        backingDuration = obj.duration
        semitone = obj.semitone
        volume = obj.volume.coerceAtLeast(0)
        track = obj.track
        metadata = obj.metadata ?: mapOf()
    }

    constructor(obj: CuePointerObject) : this(obj, null)

    constructor(id: String, beat: Float, duration: Float = 0f, semitone: Int = 0, track: Int = 0,
                volume: Int = IVolumetric.DEFAULT_VOLUME,
                metadata: Map<String, Any?> = mapOf()) {
        this.id = id
        this.beat = beat
        this.backingDuration = duration
        this.semitone = semitone
        this.track = track
        this.volume = volume.coerceAtLeast(0)
        this.metadata = metadata
    }


}