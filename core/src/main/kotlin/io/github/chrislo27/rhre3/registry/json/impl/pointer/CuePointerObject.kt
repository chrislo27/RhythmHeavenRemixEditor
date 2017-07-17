package io.github.chrislo27.rhre3.registry.json.impl.pointer

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.chrislo27.rhre3.registry.json.Verifiable


open class CuePointerObject : Verifiable {

    lateinit var id: String
    var beat: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var duration: Float = 0f
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var semitone: Int = 0
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var track: Int = 0

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        if (beat < 0f) {
            builder.append("Beat $beat is less or equal to zero\n")
        }

        if (track < 0) {
            builder.append("Track $track is negative")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }
}