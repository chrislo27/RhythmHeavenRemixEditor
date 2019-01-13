package io.github.chrislo27.rhre3.registry.datamodel

import io.github.chrislo27.rhre3.modding.ModdingMetadata
import io.github.chrislo27.rhre3.modding.ModdingUtils


interface ModdingModel<D : Datamodel> {

    val moddingMetadata: Map<ModdingUtils.Game, ModdingMetadata>

}