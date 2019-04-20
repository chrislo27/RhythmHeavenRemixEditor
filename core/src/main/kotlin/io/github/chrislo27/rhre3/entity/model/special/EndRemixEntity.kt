package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EndRemixEntity(remix: Remix, datamodel: EndRemix) : ModelEntity<EndRemix>(remix, datamodel) {

    override val supportsCopying: Boolean = false
    override val glassEffect: Boolean = false

    init {
        this.bounds.height = remix.trackCount.toFloat()
    }

    fun onTrackSizeChange(newSize: Int) {
        this.bounds.height = newSize.toFloat()
    }

    // not used
    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.trackLine
    }

    override fun renderWithGlass(editor: Editor, batch: SpriteBatch, glass: Boolean) {
        val oldColor = batch.packedColor
        val theme = editor.theme
        val selectionTint = theme.entities.selectionTint

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        batch.setColorWithTintIfNecessary(selectionTint, theme.trackLine)
        batch.fillRect(x, y, width * 0.125f, height)
        batch.fillRect(x + width, y, width * -0.5f, height)

        batch.packedColor = oldColor
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun copy(remix: Remix): EndRemixEntity {
        error("This entity does not support copying")
    }
}