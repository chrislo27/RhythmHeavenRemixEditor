package io.github.chrislo27.rhre3.sfxdb.datamodel

import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer


interface ContainerModel {

    val cues: List<CuePointer>

}