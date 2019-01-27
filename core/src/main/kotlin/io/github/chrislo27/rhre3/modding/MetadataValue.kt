package io.github.chrislo27.rhre3.modding

import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.entity.Entity


sealed class MetadataValue(val needsEntity: Boolean) {
    abstract fun getValue(entity: Entity?): String
}

class StaticValue(private val value: String) : MetadataValue(false) {
    override fun getValue(entity: Entity?): String = value
}

class WidthRangeValue : MetadataValue(true) {
    companion object {
        val EPSILON: Float = 0.0001f
        val REGEX: Regex = "(?:\\s+)?(\\d+(?:\\.\\d+)?)(?:\\s+)?(?:\\.){2}(?:\\s+)?(\\d+(?:\\.\\d+)?)(?:\\s+)?".toRegex()
    }

    var elseValue: String = ""
    val exactValues: LinkedHashMap<ClosedRange<Float>, String> = linkedMapOf()

    override fun getValue(entity: Entity?): String {
        if (entity == null) return elseValue
        val width = entity.bounds.width

        for ((range, value) in exactValues) {
            val start = range.start
            val end = range.endInclusive
            if (start == end) {
                // Check using epsilon
                if (MathUtils.isEqual(start, width, EPSILON)) return value
            } else {
                if (width in start..end) return value
            }
        }

        return elseValue
    }
}