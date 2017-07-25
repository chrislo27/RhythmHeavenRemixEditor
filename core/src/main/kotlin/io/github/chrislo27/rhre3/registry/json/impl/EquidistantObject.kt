package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.NamedID

@JsonTypeName("equidistant")
class EquidistantObject : NamedID() {

    var distance: Float = 0f
    var stretchable: Boolean = false
    lateinit var cues: List<CuePointerObject>

}