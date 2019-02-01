package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.util.Semitones


interface IRepitchable {

    companion object {

        fun anyInModel(model: ContainerModel): Lazy<Boolean> {
            return lazy {
                model.cues.any {
                    (GameRegistry.data.objectMap[it.id] as? Cue)?.repitchable == true
                }
            }
        }

        val DEFAULT_RANGE: IntRange = -(Semitones.SEMITONES_IN_OCTAVE * 2)..(Semitones.SEMITONES_IN_OCTAVE * 2)
    }

    var semitone: Int
    val canBeRepitched: Boolean
    val semitoneRange: IntRange
        get() = DEFAULT_RANGE
    val rangeWrapsAround: Boolean
        get() = false
    val persistSemitoneData: Boolean
        get() = true

}