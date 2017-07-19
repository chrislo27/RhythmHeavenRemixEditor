package io.github.chrislo27.rhre3.registry.json.impl.pointer

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.chrislo27.rhre3.registry.json.Verifiable


class EquidistantCuePointerObject : Verifiable {

    lateinit var id: String

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var track: Int = 0

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var semitone: Int = 0

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        if (track < 0) {
            builder.append("Track $track is negative")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }

}