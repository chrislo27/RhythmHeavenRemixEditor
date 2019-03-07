package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Pattern
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class PatternEntity(remix: Remix, datamodel: Pattern)
    : MultipartEntity<Pattern>(remix, datamodel), IStretchable {

    override val canBeRepitched: Boolean by IRepitchable.anyInModel(datamodel)
    override val isStretchable: Boolean = datamodel.stretchable

    init {
        datamodel.cues.mapTo(internal) { pointer ->
            GameRegistry.data.objectMap[pointer.id]?.createEntity(remix, pointer)?.apply {
                this.updateBounds {
                    this.bounds.x = this@PatternEntity.bounds.x + pointer.beat
                    this.bounds.y = this@PatternEntity.bounds.y + pointer.track
                    this.bounds.width = pointer.duration
                }
            } ?: error("Object with id ${pointer.id} not found")
        }

        this.bounds.width = internal
                .maxBy { it.bounds.x + it.bounds.width }?.run { this.bounds.x + this.bounds.width - this@PatternEntity.bounds.x } ?: error("Nothing in internal cache")
        this.bounds.height = internal
                .maxBy { it.bounds.y + it.bounds.height }?.run { this.bounds.y + this.bounds.height - this@PatternEntity.bounds.y } ?: error("Nothing in internal cache")
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.pattern
    }

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds, changeWidths = true, scaleBeats = true)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        if (!datamodel.stretchable) {
            if (bounds.width != datamodel.duration) {
                // The new duration overrides the persisted one if the pattern is not stretchable
                updateBounds {
                    bounds.width = datamodel.duration
                }
            }
        }
    }

    override fun copy(remix: Remix): PatternEntity {
        return PatternEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@PatternEntity.bounds)
            }
            it.semitone = this.semitone
            it.volumePercent = this.volumePercent
        }
    }
}