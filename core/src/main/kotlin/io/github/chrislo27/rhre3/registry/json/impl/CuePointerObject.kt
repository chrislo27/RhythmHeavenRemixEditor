package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonInclude


class CuePointerObject {

    lateinit var id: String
    var beat: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var duration: Float = 0f
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var semitone: Int = 0
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var track: Int = 0

}
