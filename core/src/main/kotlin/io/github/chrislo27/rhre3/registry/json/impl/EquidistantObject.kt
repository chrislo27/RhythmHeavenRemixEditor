package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable
import io.github.chrislo27.rhre3.registry.json.impl.pointer.EquidistantCuePointerObject

@JsonTypeName("equidistant")
class EquidistantObject : NamedIDObject(), Verifiable {

    var distance: Float = -1f
    var stretchable: Boolean = false
    lateinit var cues: List<EquidistantCuePointerObject>

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        name

        if (distance <= 0f) {
            builder.append("Distance $distance is negative")
        }

        if (cues.isEmpty()) {
            builder.append("Cues array is empty")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }
}
