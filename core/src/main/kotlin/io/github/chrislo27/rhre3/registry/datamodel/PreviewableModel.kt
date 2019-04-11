package io.github.chrislo27.rhre3.registry.datamodel

import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer


interface PreviewableModel {

    companion object {
        fun determineFromCuePointers(list: List<CuePointer>): Boolean {
            return list.mapNotNull { GameRegistry.data.objectMap[it.id] }.filterIsInstance<PreviewableModel>().any { it.canBePreviewed }
        }
    }

    val canBePreviewed: Boolean get() = true

}
