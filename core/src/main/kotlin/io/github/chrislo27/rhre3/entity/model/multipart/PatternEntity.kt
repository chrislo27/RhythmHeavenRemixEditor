package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Pattern
import io.github.chrislo27.rhre3.track.Remix


class PatternEntity(remix: Remix, datamodel: Pattern)
    : MultipartEntity<Pattern>(remix, datamodel), IStretchable {

    override val canBeRepitched: Boolean by IRepitchable.anyInModel(datamodel)
    override val isStretchable: Boolean = datamodel.stretchable

    init {
        datamodel.cues.mapTo(internal) { pointer ->
            GameRegistry.data.objectMap[pointer.id]?.createEntity(remix)?.apply {
                this.bounds.x = this@PatternEntity.bounds.x + pointer.beat
                this.bounds.y = this@PatternEntity.bounds.y + pointer.track
                this.bounds.width = pointer.duration

                // apply cue pointer settings
                (this as? IRepitchable)?.semitone = pointer.semitone
            } ?: error("Object with id ${pointer.id} not found")
        }

        this.bounds.width = internal
                .maxBy { it.bounds.x + it.bounds.width }?.run { this.bounds.x + this.bounds.width - this@PatternEntity.bounds.x } ?:
                error("Nothing in internal cache")
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.pattern
    }

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds, changeWidths = true)
    }

    override fun copy(remix: Remix): PatternEntity {
        return PatternEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@PatternEntity.bounds)
            }
            it.semitone = this.semitone
        }
    }
}