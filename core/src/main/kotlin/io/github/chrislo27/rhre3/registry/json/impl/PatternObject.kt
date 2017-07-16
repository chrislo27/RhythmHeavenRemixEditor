package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.IdentifiableObject
import io.github.chrislo27.rhre3.registry.json.NamedIDObject

@JsonTypeName("pattern")
class PatternObject : NamedIDObject() {


    inner class PatternCueObject : IdentifiableObject() {

    }

}