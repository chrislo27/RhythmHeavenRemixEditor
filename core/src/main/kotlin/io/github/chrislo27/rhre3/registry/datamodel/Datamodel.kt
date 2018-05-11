package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


abstract class Datamodel(val game: Game, val id: String, val deprecatedIDs: List<String>, val name: String)
    : Disposable {

    abstract fun createEntity(remix: Remix, cuePointer: CuePointer?): ModelEntity<*>

    var hidden: Boolean = false
    open val pickerName: String = name
    val newlinedName: String by lazy { name.replace(" - ", "\n") }
    @Suppress("LeakingThis")
    val possibleBaseBpm: ClosedRange<Float>? by lazy(this::checkBaseBpm)

    fun checkBaseBpm(): ClosedRange<Float>? {
        if (this is Cue && this.usesBaseBpm)
            return this.baseBpm..this.baseBpm

        if (this is ContainerModel) {
            val ranges = this.cues.mapNotNull { GameRegistry.data.objectMap[it.id] }
                    .mapNotNull(Datamodel::checkBaseBpm)
            if (ranges.isEmpty())
                return null
            val lower = ranges.minBy(ClosedRange<Float>::start)?.start ?: 0f
            if (lower <= 0f)
                return null
            val upper = ranges.maxBy(ClosedRange<Float>::endInclusive)?.endInclusive ?: 0f
            if (upper <= 0f)
                return null

            return lower..upper
        }

        return null
    }

}