package io.github.chrislo27.rhre3.sfxdb.datamodel

import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.SpecialDatamodel
import io.github.chrislo27.rhre3.track.Remix


abstract class Datamodel(val game: Game, val id: String, val deprecatedIDs: List<String>, val name: String,
                         open val duration: Float, val subtext: String = "")
    : Disposable {

    abstract fun createEntity(remix: Remix, cuePointer: CuePointer?): ModelEntity<*>

    var hidden: Boolean = false
    open val pickerName: PickerName = PickerName(name, subtext)
    val newlinedName: String by lazy { name.replace(" - ", "\n") }
    @Suppress("LeakingThis")
    val possibleBaseBpm: ClosedRange<Float>? by lazy(this::checkBaseBpm)
    val isSpecial: Boolean get() = game.isSpecial || this is SpecialDatamodel
    open val hideInPresentationMode: Boolean = false

    constructor(game: Game, id: String, deprecatedIDs: List<String>, name: String, subtext: String)
            : this(game, id, deprecatedIDs, name, 1.0f, subtext)

    fun checkBaseBpm(): ClosedRange<Float>? {
        if (this is Cue && this.usesBaseBpm)
            return this.baseBpm..this.baseBpm

        if (this is ContainerModel) {
            val ranges = this.cues.mapNotNull { SFXDatabase.data.objectMap[it.id] }
                    .mapNotNull(Datamodel::checkBaseBpm)
            if (ranges.isEmpty())
                return null
            val lower = ranges.minByOrNull(ClosedRange<Float>::start)?.start ?: 0f
            if (lower <= 0f)
                return null
            val upper = ranges.maxByOrNull(ClosedRange<Float>::endInclusive)?.endInclusive ?: 0f
            if (upper <= 0f)
                return null

            return lower..upper
        }

        return null
    }

}

data class PickerName(val main: String, val sub: String = "")
