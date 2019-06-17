package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.ILoadsSounds
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.sfxdb.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.RandomCue
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.random


class RandomCueEntity(remix: Remix, datamodel: RandomCue)
    : MultipartEntity<RandomCue>(remix, datamodel) {

    init {
        bounds.width = datamodel.cues.map(CuePointer::duration).max() ?: error(
                "Datamodel ${datamodel.id} has no internal cues")
    }

    private fun getPossibleObjects(): List<Pair<Datamodel, CuePointer>> {
        return datamodel.cues.mapNotNull { pointer ->
            GameRegistry.data.objectMap[pointer.id]?.let { it to pointer }
        }
    }

    private fun reroll() {
        val thisSemitone = semitone
        val thisVolume = volumePercent

        // Set semitone and volume to zero, repopulate, and reset it so the setters in MultipartEntity take effect
        semitone = 0
        volumePercent = IVolumetric.DEFAULT_VOLUME

        internal.clear()
        internal +=
                getPossibleObjects().takeIf {
                    it.isNotEmpty()
                }?.random()?.let { pair ->
                    pair.first.createEntity(remix, pair.second).also { ent ->
                        ent.updateBounds {
                            ent.bounds.x = this@RandomCueEntity.bounds.x
                            ent.bounds.width = this@RandomCueEntity.bounds.width
                            ent.bounds.y = this@RandomCueEntity.bounds.y
                        }
                    }
                } ?: error("No valid entities found from randomization for object ${datamodel.id}")

        // Re-set semitone and volume so it takes effect in the internals
        semitone = thisSemitone
        volumePercent = thisVolume
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.pattern
    }

    override fun loadSounds() {
        super.loadSounds()
        getPossibleObjects()
                .map { it.first.createEntity(remix, null) }
                .forEach {
                    if (it is ILoadsSounds) {
                        it.loadSounds()
                    }
                }
    }

    override fun unloadSounds() {
        super.unloadSounds()
        getPossibleObjects()
                .map { it.first.createEntity(remix, null) }
                .forEach {
                    if (it is ILoadsSounds) {
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