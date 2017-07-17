package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable

@JsonTypeName("cue")
open class CueObject : NamedIDObject(), Verifiable {

    var duration: Float = -1f

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var stretchable: Boolean = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var repitchable: Boolean = false

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var fileExtension: String = "ogg"

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        name

        if (duration <= 0) {
            builder.append("Duration $duration is negative")
        }

        if (fileExtension !in RHRE3.SUPPORTED_SOUND_TYPES) {
            builder.append("File extension $fileExtension isn't a supported sound type (${RHRE3.SUPPORTED_SOUND_TYPES})")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }
}