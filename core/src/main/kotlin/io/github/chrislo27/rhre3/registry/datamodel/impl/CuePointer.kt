package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.registry.json.CuePointerObject

fun CuePointerObject.toDatamodel(): CuePointer = CuePointer(
        this)

fun List<CuePointerObject>.mapToDatamodel(): List<CuePointer> = this.map(::CuePointer)

class CuePointer {

    val id: String
    val beat: Float

    val backingDuration: Float
    val semitone: Int
    val track: Int

    val duration: Float
        get() =
            if (backingDuration <= 0f) {
                (GameRegistry.data.objectMap[id] as? DurationModel)?.duration ?: error("$id is not a DurationModel")
            } else {
                backingDuration
            }

    val metadata: Map<String, Any?>

    constructor(obj: CuePointerObject) {
        id = obj.id
        beat = obj.beat
        backingDuration = obj.duration
        semitone = obj.semitone
        track = obj.track
        metadata = obj.metadata ?: mapOf()
    }

    constructor(id: String, beat: Float, duration: Float = 0f, semitone: Int = 0, track: Int = 0,
                metadata: Map<String, Any?> = mapOf()) {
        this.id = id
        this.beat = beat
        this.backingDuration = duration
        this.semitone = semitone
        this.track = track
        this.metadata = mapOf()
    }


}