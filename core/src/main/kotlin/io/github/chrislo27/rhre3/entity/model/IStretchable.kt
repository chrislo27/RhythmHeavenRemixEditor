package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue


interface IStretchable {

    companion object {
        const val STRETCH_AREA: Float = 1f / 12f
        const val MIN_STRETCH: Float = 1f / 8f

        fun anyInModel(model: ContainerModel): Lazy<Boolean> {
            return lazy {
                model.cues.any {
                    (GameRegistry.data.objectMap[it.id] as? Cue)?.stretchable == true
                }
            }
        }
    }

    val isStretchable: Boolean

}