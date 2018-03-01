package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul


abstract class ModelEntity<out M : Datamodel>(remix: Remix, val datamodel: M)
    : Entity(remix) {

    companion object {
        const val BORDER: Float = 4f
        const val JSON_DATAMODEL = "datamodel"
    }

    final override val jsonType: String = "model"
    open val renderText: String
        get() = datamodel.newlinedName
    val isSpecialEntity: Boolean = datamodel.game === GameRegistry.data.specialGame

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)

        objectNode.put(JSON_DATAMODEL, datamodel.id)
        if (datamodel.game.isCustom) {
            objectNode.put("isCustom", true)
        }
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
    }

    abstract fun getRenderColor(): Color

    protected open fun renderBeforeText(batch: SpriteBatch) {

    }

    protected open fun getTextForSemitone(semitone: Int): String {
        return Semitones.getSemitoneName(semitone)
    }

    override fun render(batch: SpriteBatch) {
        val game = datamodel.game
        val text = renderText
        val font = remix.main.defaultFont
        val color = getRenderColor()
        val oldColor = batch.packedColor
        val oldFontSizeX = font.data.scaleX
        val oldFontSizeY = font.data.scaleY
        val selectionTint = remix.editor.theme.entities.selectionTint

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        // filled rect + border
        batch.setColorWithTintIfNecessary(selectionTint, color)
        batch.fillRect(x, y,
                       width, height)

        batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                                          (color.g - 0.25f).coerceIn(0f, 1f),
                                          (color.b - 0.25f).coerceIn(0f, 1f),
                                          color.a)

        if (this is IStretchable && this.isStretchable) {
            val oldColor = batch.packedColor
            val arrowWidth: Float = Math.min(bounds.width / 2f, Editor.ENTITY_HEIGHT / Editor.ENTITY_WIDTH)
            val y = bounds.y + bounds.height / 2 - 0.5f
            val arrowTex = AssetRegistry.get<Texture>("entity_stretchable_arrow")

            batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                                              (color.g - 0.25f).coerceIn(0f, 1f),
                                              (color.b - 0.25f).coerceIn(0f, 1f),
                                              color.a * 0.5f)

            batch.draw(arrowTex, x + arrowWidth, y, width - arrowWidth * 2, 1f,
                       arrowTex.width / 2, 0, arrowTex.width / 2,
                       arrowTex.height, false, false)
            batch.draw(arrowTex, x, y, arrowWidth, 1f,
                       0, 0, arrowTex.width / 2, arrowTex.height, false, false)
            batch.draw(arrowTex, x + bounds.width, y, -arrowWidth, 1f,
                       0, 0, arrowTex.width / 2, arrowTex.height, false, false)

            batch.setColor(oldColor)
        }

        batch.drawRect(x, y,
                       width, height,
                       remix.editor.toScaleX(BORDER), remix.editor.toScaleY(BORDER))

        renderBeforeText(batch)

        batch.setColor(1f, 1f, 1f, 0.5f)
        val iconSizeY = 1f - 4 * (remix.editor.toScaleY(BORDER))
        val iconSizeX = remix.editor.toScaleX(iconSizeY * Editor.ENTITY_HEIGHT)

        batch.draw(game.icon,
                   x + 2 * (remix.editor.toScaleX(BORDER)),
                   y + 2 * remix.editor.toScaleY(BORDER) + ((height - 4 * remix.editor.toScaleY(
                           BORDER)) - iconSizeY) / 2,
                   iconSizeX, iconSizeY)

        batch.setColor(oldColor)
        val oldFontColor = font.color
        val fontScale = 0.6f
        font.color = remix.editor.theme.entities.nameColor
        font.data.setScale(oldFontSizeX * fontScale, oldFontSizeY * fontScale)
        // width - iconSizeX - 6 * (remix.editor.toScaleX(BORDER))
        val allottedWidth = width - 2 * (remix.editor.toScaleX(BORDER))
        //        val allottedHeight = height - 4 * (remix.editor.toScaleY(BORDER))
        fun computeHeight(): Float =
                font.getTextHeight(text, allottedWidth, true)

//        val textHeight = computeHeight()
        val textX = x + 1 * (remix.editor.toScaleX(BORDER))
        val textY = y + height / 2
//        if (textHeight > allottedHeight) {
//            val ratio = allottedHeight / (textHeight - (font.lineHeight - font.capHeight))
//            font.data.setScale(ratio * font.data.scaleX, ratio * font.data.scaleY)
//        }
        font.draw(batch, text, textX, textY + computeHeight() / 2, allottedWidth, Align.right, true)

        if (this is IRepitchable && (this.canBeRepitched || this.semitone != 0)) {
            val borderedFont = remix.main.defaultBorderedFont
            remix.editor.apply {
                borderedFont.scaleFont(remix.camera)
            }
            borderedFont.scaleMul(0.75f)
            if (!this.canBeRepitched) {
                borderedFont.setColor(1f, 0f, 0f, 1f)
            } else {
                borderedFont.setColor(1f, 1f, 1f, 1f)
            }
            borderedFont.draw(batch, getTextForSemitone(this.semitone),
                              x + 2 * remix.editor.toScaleX(BORDER),
                              y + 2 * remix.editor.toScaleY(BORDER) + borderedFont.capHeight)
            remix.editor.apply {
                borderedFont.unscaleFont()
            }
        }
        font.color = oldFontColor
        font.data.setScale(oldFontSizeX, oldFontSizeY)
    }
}