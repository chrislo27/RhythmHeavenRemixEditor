package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


abstract class ModelEntity<out M : Datamodel>(remix: Remix, val datamodel: M) : Entity(remix) {

    companion object {
        const val BORDER: Float = 4f
    }

    abstract fun getRenderColor(): Color

    override fun render(batch: SpriteBatch) {
        val game = datamodel.game
        val text = datamodel.name
        val font = remix.main.defaultFont
        val color = getRenderColor()
        val oldColor = batch.packedColor
        val oldFontSize = font.data.scaleX
        val selectionTint = remix.editor.theme.entities.selectionTint

        fun SpriteBatch.setColorWithSelectionIfNecessary(r: Float, g: Float, b: Float, a: Float) {
            if (isSelected) {
                this.setColor((r * (1 + selectionTint.r)).coerceIn(0f, 1f),
                              (g * (1 + selectionTint.g)).coerceIn(0f, 1f),
                              (b * (1 + selectionTint.b)).coerceIn(0f, 1f),
                              a)
            } else {
                this.setColor(r, g, b, a)
            }
        }
        fun SpriteBatch.setColorWithSelectionIfNecessary(color: Color) {
            this.setColorWithSelectionIfNecessary(color.r, color.g, color.b, color.a)
        }

        // filled rect + border
        batch.setColorWithSelectionIfNecessary(color)
        batch.fillRect(bounds.x * Editor.ENTITY_WIDTH, bounds.y * Editor.ENTITY_HEIGHT,
                       bounds.width * Editor.ENTITY_WIDTH, bounds.height * Editor.ENTITY_HEIGHT)
        batch.setColorWithSelectionIfNecessary((color.r - 0.25f).coerceIn(0f, 1f),
                                               (color.g - 0.25f).coerceIn(0f, 1f),
                                               (color.b - 0.25f).coerceIn(0f, 1f),
                                               color.a)
        batch.drawRect(bounds.x * Editor.ENTITY_WIDTH, bounds.y * Editor.ENTITY_HEIGHT,
                       bounds.width * Editor.ENTITY_WIDTH, bounds.height * Editor.ENTITY_HEIGHT,
                       BORDER)

        batch.setColor(1f, 1f, 1f, 0.5f)

        batch.setColor(oldColor)
        font.data.setScale(oldFontSize)
    }
}