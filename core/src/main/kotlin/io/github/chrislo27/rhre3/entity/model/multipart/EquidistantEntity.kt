package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Equidistant
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class EquidistantEntity(remix: Remix, datamodel: Equidistant)
    : MultipartEntity<Equidistant>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = datamodel.stretchable
    override val shouldRenderInternal = true

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds)
        var currentRelative: Float = 0f
        internal.forEachIndexed { index, it ->
            val track = (it.bounds.y - this.bounds.y).toInt()

            if (track == 0 && index > 0) {
                currentRelative += this@EquidistantEntity.bounds.width
            }

            it.updateBounds {
                it.bounds.x = this@EquidistantEntity.bounds.x + currentRelative
                it.bounds.width = this@EquidistantEntity.bounds.width
                it.bounds.y = this@EquidistantEntity.bounds.y + track.toFloat()
            }
        }
    }

    init {
        datamodel.cues.mapIndexedTo(internal) { index, pointer ->
            SFXDatabase.data.objectMap[pointer.id]?.createEntity(remix, pointer)?.apply {
                this.updateBounds {
                    this@apply.bounds.x = this@EquidistantEntity.bounds.x
                    this@apply.bounds.y = this@EquidistantEntity.bounds.y + pointer.track
                    this@apply.bounds.width = this@EquidistantEntity.bounds.width
                }
            } ?: error("Object with id ${pointer.id} not found")
        }
        this.bounds.width = datamodel.duration
        updateInternalCache(bounds)
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.equidistant
    }

    override fun copy(remix: Remix): EquidistantEntity {
        return EquidistantEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@EquidistantEntity.bounds)
            }
            it.semitone = this.semitone
            it.volumePercent = this.volumePercent
        }
    }
}