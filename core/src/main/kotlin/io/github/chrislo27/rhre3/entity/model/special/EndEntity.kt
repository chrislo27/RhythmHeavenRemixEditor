package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EndEntity(remix: Remix, datamodel: EndRemix) : ModelEntity<EndRemix>(remix, datamodel) {

    override val supportsCopying: Boolean = false

    init {
        this.bounds.height = Editor.TRACK_COUNT.toFloat()
        this.bounds.width = 0.125f
    }

    // not used
    override fun getRenderColor(): Color {
        return remix.editor.theme.trackLine
    }

    override fun render(batch: SpriteBatch) {
        val oldColor = batch.packedColor
        val selectionTint = remix.editor.theme.entities.selectionTint

        batch.setColorWithTintIfNecessary(selectionTint, remix.editor.theme.trackLine)
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