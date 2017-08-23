package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight


abstract class ModelEntity<out M : Datamodel>(remix: Remix, val datamodel: M) : Entity(remix) {

    companion object {
        const val BORDER: Float = 4f
        const val JSON_DATAMODEL = "datamodel"
    }

    override final val jsonType: String = "model"

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)

        objectNode.put(JSON_DATAMODEL, datamodel.id)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
    }

    abstract fun getRenderColor(): Color

    protected open fun renderBeforeText(batch: SpriteBatch) {

    }

    override fun render(batch: SpriteBatch) {
        val game = datamodel.game
        val text = datamodel.newlinedName
        val font = remix.main.defaultFont
        val color = getRenderColor()
        val oldColor = batch.packedColor
        val oldFontSizeX = font.data.scaleX
        val oldFontSizeY = font.data.scaleY
        val selectionTint = remix.editor.theme.entities.selectionTint

        // filled rect + border
        batch.setColorWithTintIfNecessary(selectionTint, color)
        batch.fillRect(bounds.x, bounds.y,
                       bounds.width, bounds.height)
        batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                                          (color.g - 0.25f).coerceIn(0f, 1f),
                                          (color.b - 0.25f).coerceIn(0f, 1f),
                                          color.a)
        batch.drawRect(bounds.x, bounds.y,
                       bounds.width, bounds.height,
                       remix.editor.toScaleX(BORDER), remix.editor.toScaleY(BORDER))

        renderBeforeText(batch)

        batch.setColor(1f, 1f, 1f, 0.5f)
        val iconSizeY = 1f - 4 * (remix.editor.toScaleY(BORDER))
        val iconSizeX = remix.editor.toScaleX(iconSizeY * Editor.ENTITY_HEIGHT)

        batch.draw(game.icon,
                   bounds.x + 2 * (remix.editor.toScaleX(BORDER)),
                   bounds.y + 2 * remix.editor.toScaleY(BORDER) + ((bounds.height - 4 * remix.editor.toScaleY(BORDER)) - iconSizeY) / 2,
                   iconSizeX, iconSizeY)

        batch.setColor(oldColor)
        val oldFontColor = font.color
        val fontScale = 0.5f
        font.color = remix.editor.theme.entities.nameColor
        font.data.setScale(oldFontSizeX * fontScale, oldFontSizeY * fontScale)
        val allottedWidth = bounds.width - iconSizeX - 6 * (remix.editor.toScaleX(BORDER))
        val allottedHeight = bounds.height - 4 * (remix.editor.toScaleY(BORDER))
        fun computeHeight(): Float =
                font.getTextHeight(text, allottedWidth, true)

        val textHeight = computeHeight()
        val textX = bounds.x + iconSizeX + 4 * (remix.editor.toScaleX(BORDER))
        val textY = bounds.y + bounds.height / 2
//        if (textHeight > allottedHeight) {
//            val ratio = allottedHeight / (textHeight - (font.lineHeight - font.capHeight))
//            font.data.setScale(ratio * font.data.scaleX, ratio * font.data.scaleY)
//        }
        font.draw(batch, text, textX, textY + computeHeight() / 2, allottedWidth, Align.right, true)

        if (this is IRepitchable && (this.canBeRepitched || this.semitone != 0)) {
            if (!this.canBeRepitched) {
                font.setColor(1f, 0f, 0f, 1f)
            }
            font.draw(batch, Semitones.getSemitoneName(this.semitone),
                      bounds.x + 2 * remix.editor.toScaleX(BORDER),
                      bounds.y + 2 * remix.editor.toScaleY(BORDER) + font.capHeight)
        }
        font.color = oldFontColor
        font.data.setScale(oldFontSizeX, oldFontSizeY)
    }
}