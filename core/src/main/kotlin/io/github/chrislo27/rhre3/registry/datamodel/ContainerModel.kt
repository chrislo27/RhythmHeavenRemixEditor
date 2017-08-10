package io.github.chrislo27.rhre3.registry.datamodel

import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer


interface ContainerModel {

    val cues: List<CuePointer>

}