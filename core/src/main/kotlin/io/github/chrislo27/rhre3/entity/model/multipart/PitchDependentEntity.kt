package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ISoundDependent
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.PitchDependent
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.PitchRange
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class PitchDependentEntity(remix: Remix, datamodel: PitchDependent)
    : MultipartEntity<PitchDependent>(remix, datamodel), IStretchable {

    private data class DatamodelPointer(val datamodel: Datamodel, val ptr: CuePointer, val bounds: PitchRange)
    
    private val possibleObjects: List<DatamodelPointer> = datamodel.intervals.mapNotNull { (interval, pointer) ->
        SFXDatabase.data.objectMap[pointer.id]?.let { DatamodelPointer(it, pointer, interval) }
    }
    private val createdEntities: Map<DatamodelPointer, Entity> = possibleObjects.associateWith { it.datamodel.createEntity(remix, it.ptr.copy(beat = 0f)) }

    override val isStretchable: Boolean = datamodel.cues.mapNotNull { SFXDatabase.data.objectMap[it.id] }.any { it is Cue && it.stretchable }
    
    init {
        bounds.width = datamodel.cues.map(CuePointer::duration).max() ?: error(
                "PitchDependent datamodel ${datamodel.id} has no internal cues")
    }

    private fun pickSound() {
        val thisSemitone = semitone
        val thisVolume = volumePercent

        // Set semitone and volume to zero, repopulate, and reset it so the setters in MultipartEntity take effect
        semitone = 0
        volumePercent = IVolumetric.DEFAULT_VOLUME

        internal.clear()
        if (createdEntities.isEmpty()) {
            error("No valid entities found for pitch picking for PitchDependent ${datamodel.id}")
        }
        internal +=
                (createdEntities.entries.firstOrNull { thisSemitone in it.key.bounds } ?: createdEntities.entries.first()).let { (_, ent) ->
                    ent.updateBounds {
                        ent.bounds.x = this.bounds.x
                        ent.bounds.width = this.bounds.width
                        ent.bounds.y = this.bounds.y
                    }
                    ent
                }

        // Re-set semitone and volume so it takes effect in the internals
        semitone = thisSemitone
        volumePercent = thisVolume
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.pattern
    }

    override fun onPreloadSounds() {
        createdEntities
                .forEach {
                    if (it is ISoundDependent) {
                        it.preloadSounds()
                    }
                }
    }
    
    override fun onUnloadSounds() {
        createdEntities
                .forEach {
                    if (it is ISoundDependent) {
                        it.unloadSounds()
                    }
                }
    }

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds, changeWidths = true, scaleBeats = true)
        if (internal.isEmpty())
            pickSound()
    }

    override fun onStart() {
        pickSound()
        super.onStart()
    }

    override fun copy(remix: Remix): PitchDependentEntity {
        return PitchDependentEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this.bounds)
            }
            it.semitone = this.semitone
            it.volumePercent = this.volumePercent
        }
    }
}