package io.github.chrislo27.rhre3.sfxdb.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.PitchDependentEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.ContainerModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.PreviewableModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel
import io.github.chrislo27.rhre3.track.Remix
import java.util.*


class PitchDependent(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                     intervals: Map<PitchRange, String>,
                     override val responseIDs: List<String>)
    : Datamodel(game, id, deprecatedIDs, name), ResponseModel, PreviewableModel, ContainerModel {
    
    override val canBePreviewed: Boolean by lazy { intervals.isNotEmpty() /* Interval values must be IDs to Cues */ }
    val intervals: NavigableMap<PitchRange, CuePointer> = TreeMap(intervals.mapValues { (interval, id) ->
        CuePointer(id, 0f, metadata = mapOf("interval" to interval))
    })
    override val cues: List<CuePointer> = this.intervals.values.toList()
    
    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): PitchDependentEntity {
        return PitchDependentEntity(remix, this)
    }
    
    override fun dispose() {
    }
    
}

data class PitchRange(val lower: Int, val upper: Int): Comparable<PitchRange> {
    
    companion object {
        val REGEX: Regex = """([+-]?\d+)?\s*\.\.\s*([+-]?\d+)?""".toRegex()
        val INVALID: PitchRange = PitchRange(Int.MIN_VALUE, Int.MAX_VALUE)
        
        fun parseFromString(str: String): PitchRange {
            val match = REGEX.matchEntire(str.trim()) ?: return INVALID
            val lower: Int = match.groupValues[1].replace("+", "").toIntOrNull() ?: Int.MIN_VALUE
            val upper: Int = match.groupValues[2].replace("+", "").toIntOrNull() ?: Int.MAX_VALUE
            return PitchRange(lower, upper)
        }
    }
    
    val isInvalid: Boolean = (lower == Int.MIN_VALUE && upper == Int.MAX_VALUE) || upper < lower
    val isUnbounded: Boolean = lower == Int.MIN_VALUE || upper == Int.MAX_VALUE
    
    operator fun contains(value: Int): Boolean {
        if (isInvalid) return false
        return value in lower..upper
    }
    
    fun intersects(other: PitchRange): Boolean {
        if (isInvalid || other.isInvalid) return false
        return this.lower <= other.upper && other.lower <= this.upper
    }
    
    override fun compareTo(other: PitchRange): Int {
        return this.lower.compareTo(other.lower)
    }
    
    override fun toString(): String {
        if (isInvalid) return "INVALID_RANGE"
        return "${if (lower == Int.MIN_VALUE) "" else "$lower"}..${if (upper == Int.MAX_VALUE) "" else "$upper"}"
    }
}