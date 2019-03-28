package io.github.chrislo27.rhre3.remixgen

import com.fasterxml.jackson.databind.node.ObjectNode


data class RemixGeneratorSettings(val seed: Long) {

    companion object {
        fun fromJson(tree: ObjectNode): RemixGeneratorSettings? {
            val seed: Long = tree["seed"]?.asLong() ?: return null
            return RemixGeneratorSettings(seed)
        }
    }

    fun toJson(tree: ObjectNode) {
        tree.put("seed", seed)
    }

    fun toDebugString(): String {
        return "  Seed: $seed"
    }
}