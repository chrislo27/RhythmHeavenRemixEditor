package io.github.chrislo27.rhre3.sfxdb.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.json.CuePointerObject

fun CuePointerObject.toDatamodel(): CuePointer = CuePointer(this)

fun List<CuePointerObject>.mapToDatamodel(): List<CuePointer> = this.map(::CuePointer)
fun List<CuePointerObject>.mapToDatamodel(starSubstitution: String): List<CuePointer> = this.map { CuePointer(it, starSubstitution) }

fun List<CuePointer>.mapToJsonObject(): List<CuePointerObject> = this.map(CuePointer::toJsonObject)
fun List<CuePointer>.mapToJsonObject(starSubstitution: String): List<CuePointerObject> = this.map(CuePointer::toJsonObject).onEach {
    if (it.id.startsWith(starSubstitution)) {
        it.id = "*" + it.id.substringAfter(starSubstitution)
    }
}

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
                SFXDatabase.data.objectMap[id]?.duration ?: error("$id does not exist")
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
    
    fun toJsonObject(): CuePointerObject {
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

    fun copy(id: String = this.id, beat: Float = this.beat, duration: Float = this.duration, semitone: Int = this.semitone, track: Int = this.track,
             volume: Int = this.volume,
             metadata: Map<String, Any?> = this.metadata): CuePointer = CuePointer(id, beat, duration, semitone, track, volume, metadata)

}