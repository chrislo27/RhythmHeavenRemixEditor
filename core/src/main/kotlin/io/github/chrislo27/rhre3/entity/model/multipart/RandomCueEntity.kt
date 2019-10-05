package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ISoundDependent
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.RandomCue
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.random


class RandomCueEntity(remix: Remix, datamodel: RandomCue)
    : MultipartEntity<RandomCue>(remix, datamodel) {

    private data class DatamodelPointer(val datamodel: Datamodel, val ptr: CuePointer)

    private val possibleObjects: List<DatamodelPointer> = datamodel.cues.mapNotNull { pointer ->
        SFXDatabase.data.objectMap[pointer.id]?.let { DatamodelPointer(it, pointer) }
    }
    private val createdEntities: List<Entity> = possibleObjects.map { it.datamodel.createEntity(remix, it.ptr.copy(beat = 0f)) }

    init {
        bounds.width = datamodel.cues.map(CuePointer::duration).max() ?: error(
                "RandomCue datamodel ${datamodel.id} has no internal cues")
    }

    private fun reroll() {
        val thisSemitone = semitone
        val thisVolume = volumePercent

        // Set semitone and volume to zero, repopulate, and reset it so the setters in MultipartEntity take effect
        semitone = 0
        volumePercent = IVolumetric.DEFAULT_VOLUME

        if (createdEntities.isEmpty()) {
            error("No valid entities found from randomization for RandomCue ${datamodel.id}")
        }

        internal.clear()
        internal +=
                createdEntities.random().also { ent ->
                    ent.updateBounds {
                        ent.bounds.x = this.bounds.x
                        ent.bounds.width = this.bounds.width
                        ent.bounds.y = this.bounds.y
                    }
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
        translateInternal(oldBounds)
        if (internal.isEmpty())
            reroll()
    }

    override fun onStart() {
        reroll()
        super.onStart()
    }

    override fun copy(remix: Remix): RandomCueEntity {
        return RandomCueEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this.bounds)
            }
            it.semitone = this.semitone
            it.volumePercent = this.volumePercent
        }
    }
}