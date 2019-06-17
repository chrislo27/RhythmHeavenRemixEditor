package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.sfxdb.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.KeepTheBeat
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeatEntity(remix: Remix, datamodel: KeepTheBeat)
    : MultipartEntity<KeepTheBeat>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = true

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.keepTheBeat
    }

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds)
        val widthChanged = oldBounds.width != bounds.width

        if (widthChanged) {
            populate()
        }
    }

    private fun populate() {
        val cues = datamodel.cues
        if (cues.isEmpty())
            error("KeepTheBeat datamodel has nothing in it")

        val sequenceLength: Float = Math.max(datamodel.duration, datamodel.totalSequenceDuration)
        val percentage: Float = bounds.width / sequenceLength
        val thisSemitone = semitone
        val thisVolume = volumePercent

        // Set semitone and volume to zero, repopulate, and reset it so the setters in MultipartEntity take effect
        semitone = 0
        volumePercent = IVolumetric.DEFAULT_VOLUME

        if (sequenceLength <= 0f)
            error("Sequence length for keep the beat cannot be negative or zero ($sequenceLength)")

        internal.clear()

        var index = 0
        while (true) {
            val cycle = index / cues.size
            val remIndex: Int = index % cues.size
            val pointer: CuePointer = cues[remIndex]
            val beat = pointer.beat + cycle * sequenceLength

            if (beat >= this.bounds.width)
                break

            internal += GameRegistry.data.objectMap[pointer.id]?.createEntity(remix, pointer)?.apply {
                this.updateBounds {
                    this@apply.bounds.x = this@KeepTheBeatEntity.bounds.x + beat
                    this@apply.bounds.y = this@KeepTheBeatEntity.bounds.y + pointer.track
                    this@apply.bounds.width = pointer.duration
                }
            } ?: error("Missing object ${pointer.id} while trying to populate keep the beat ${datamodel.id}")

            index++
        }

        // Re-set semitone and volume so it takes effect in the internals
        semitone = thisSemitone
        volumePercent = thisVolume
    }

    init {
        this.bounds.width = datamodel.duration
        populate()
    }

    override fun copy(remix: Remix): KeepTheBeatEntity {
        return KeepTheBeatEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@KeepTheBeatEntity.bounds)
            }
            it.semitone = this.semitone
            it.volumePercent = this.volumePercent
        }
    }
}