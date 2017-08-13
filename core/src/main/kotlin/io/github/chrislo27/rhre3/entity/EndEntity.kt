package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EndEntity(remix: Remix) : Entity(remix) {

    override val supportsCopying: Boolean = false

    init {
        this.bounds.height = Editor.TRACK_COUNT.toFloat()
        this.bounds.width = 0.125f
    }

    override fun render(batch: SpriteBatch) {
        val oldColor = batch.packedColor
        val selectionTint = remix.editor.theme.entities.selectionTint

        batch.setColorWithSelectionIfNecessary(selectionTint, remix.editor.theme.trackLine)
        batch.fillRect(bounds.x, bounds.y, bounds.width * 0.125f, bounds.height)
        batch.fillRect(bounds.x + bounds.width, bounds.y, bounds.width * -0.5f, bounds.height)

        batch.setColor(oldColor)
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun copy(remix: Remix): EndEntity {
        error("This entity does not support copying")
    }
}