package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EndEntity(remix: Remix, datamodel: EndRemix) : ModelEntity<EndRemix>(remix, datamodel) {

    override val supportsCopying: Boolean = false

    init {
        this.bounds.height = remix.trackCount.toFloat()
        this.bounds.width = 0.125f
    }

    fun onTrackSizeChange(newSize: Int) {
        this.bounds.height = newSize.toFloat()
    }

    // not used
    override fun getRenderColor(): Color {
        return remix.editor.theme.trackLine
    }

    override fun render(batch: SpriteBatch) {
        val oldColor = batch.packedColor
        val selectionTint = remix.editor.theme.entities.selectionTint

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        batch.setColorWithTintIfNecessary(selectionTint, remix.editor.theme.trackLine)
        batch.fillRect(x, y, width * 0.125f, height)
        batch.fillRect(x + width, y, width * -0.5f, height)

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