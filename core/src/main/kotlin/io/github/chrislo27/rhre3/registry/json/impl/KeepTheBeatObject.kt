package io.github.chrislo27.rhre3.registry.json.impl

import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable
import io.github.chrislo27.rhre3.registry.json.impl.pointer.CuePointerObject


class KeepTheBeatObject : NamedIDObject(), Verifiable {

    var duration: Float = -1f
    lateinit var cues: List<CuePointerObject>

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        name

        if (duration <= 0) {
            builder.append("Duration $duration is <= 0\n")
        }

        if (cues.isEmpty()) {
            builder.append("Cues array is empty\n")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }
}