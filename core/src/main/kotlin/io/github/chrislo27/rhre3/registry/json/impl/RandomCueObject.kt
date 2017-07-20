package io.github.chrislo27.rhre3.registry.json.impl

import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable
import io.github.chrislo27.rhre3.registry.json.impl.pointer.CuePointerObject


class RandomCueObject : NamedIDObject(), Verifiable {

    lateinit var cues: List<CuePointerObject>

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        name

        if (cues.isEmpty()) {
            builder.append("Cues array is empty\n")
        }

        return if (builder.isEmpty()) null else builder.toString()
    }
}