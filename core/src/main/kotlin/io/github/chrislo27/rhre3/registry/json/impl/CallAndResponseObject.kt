package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable
import io.github.chrislo27.rhre3.registry.json.impl.pointer.CuePointerObject

@JsonTypeName("callAndResponse")
class CallAndResponseObject : NamedIDObject(), Verifiable {

    var duration: Float = -1f

    var stretchable: Boolean = false

    lateinit var counterparts: List<List<String>>

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var middle: List<CuePointerObject> = listOf()

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var end: List<CuePointerObject> = listOf()

    override fun verify(): String? {
        val builder = StringBuilder()

        id
        name

        if (duration <= 0) {
            builder.append("Duration $duration is negative\n")
        }

        if (counterparts.isEmpty()) {
            builder.append("Counterparts array is empty\n")
        } else {
            val listOfLess = counterparts.filter { it.size < 2 }
            if (listOfLess.isNotEmpty()) {
                builder.append("Certain arrays in the counterparts array don't have enough items:\n")
                listOfLess.forEach {
                    builder.append("  -> $it\n")
                }
            }
        }

        return if (builder.isEmpty()) null else builder.toString()
    }


}