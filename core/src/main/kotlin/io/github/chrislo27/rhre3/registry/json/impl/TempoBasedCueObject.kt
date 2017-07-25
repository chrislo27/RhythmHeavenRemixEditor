package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("tempoBasedCue")
class TempoBasedCueObject : CueObject() {

    var baseBpm: Float = 0f

}