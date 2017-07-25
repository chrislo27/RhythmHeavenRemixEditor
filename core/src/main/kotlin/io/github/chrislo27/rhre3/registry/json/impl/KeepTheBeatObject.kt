package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.NamedID

@JsonTypeName("keepTheBeat")
class KeepTheBeatObject : NamedID() {

    var duration: Float = 0f
    lateinit var cues: List<CuePointerObject>

}