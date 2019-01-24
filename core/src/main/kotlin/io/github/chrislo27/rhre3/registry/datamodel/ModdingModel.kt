package io.github.chrislo27.rhre3.registry.datamodel

import io.github.chrislo27.rhre3.modding.ModdingGame
import io.github.chrislo27.rhre3.modding.ModdingMetadata


interface ModdingModel {

    val moddingMetadata: Map<ModdingGame, ModdingMetadata>

}