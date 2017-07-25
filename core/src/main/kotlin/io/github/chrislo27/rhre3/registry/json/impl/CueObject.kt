package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.NamedID

@JsonTypeName("cue")
open class CueObject : NamedID() {

    var duration: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var stretchable: Boolean = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var repitchable: Boolean = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var fileExtension: String = "ogg"

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var introSound: String? = null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var endingSound: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var responseIDs: List<String> = listOf()

}