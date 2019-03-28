package io.github.chrislo27.rhre3.remixgen

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*


class RemixGeneratorSettings(val seed: Long) {

    companion object {
        fun fromJson(tree: ObjectNode): RemixGeneratorSettings? {
            val seed: Long = tree["seed"]?.asLong() ?: return null
            return RemixGeneratorSettings(seed)
        }
    }

    val random: Random = Random(seed)

    fun toJson(tree: ObjectNode) {
        tree.put("seed", seed)
    }

    fun toDebugString(): String {
        return "  Seed: $seed"
    }

    override fun toString(): String {
        return "[seed=$seed]"
    }

}