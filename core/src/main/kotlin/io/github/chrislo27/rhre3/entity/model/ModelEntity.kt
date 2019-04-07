package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*


abstract class ModelEntity<out M : Datamodel>(remix: Remix, val datamodel: M)
    : Entity(remix) {

    companion object {
        const val BORDER: Float = 4f
        const val JSON_DATAMODEL = "datamodel"
        private val TMP_COLOR = Color(1f, 1f, 1f, 1f)
    }

    final override val jsonType: String = "model"
    open val renderText: String
        get() = datamodel.newlinedName
    open val attemptTextOnScreen: Boolean
        get() = true
    val isSpecialEntity: Boolean = datamodel.game.isSpecial
    open var needsNameTooltip: Boolean = false
        protected set

    init {
        bounds.width = datamodel.duration
    }

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

    abstract fun getRenderColor(editor: Editor, theme: Theme): Color

    protected open fun renderBeforeText(editor: Editor, batch: SpriteBatch) {

    }

    open fun getTextForSemitone(semitone: Int): String {
        return Semitones.getSemitoneName(semitone)
    }

    private fun Color.rotateColour(glow: Boolean): Color {
        val tmp = TMP_COLOR
        val coeff = if (!glow) 1f else MathUtils.lerp(0.5f, 1.25f, MathHelper.getTriangleWave(1f))
        tmp.a = a
        tmp.r = b * coeff
        tmp.g = r * coeff
        tmp.b = g * coeff
        return tmp
    }

    override fun render(editor: Editor, batch: SpriteBatch) {
        val game = datamodel.game
        val textColor = editor.theme.entities.nameColor
        val text = renderText + (if (ModdingUtils.moddingToolsEnabled && editor.currentTool == Tool.RULER) {
            GameRegistry.moddingMetadata.currentData.joinToStringFromData(datamodel, this, keyColor = "#$textColor").takeIf { it.isNotEmpty() }?.let { "\n$it" } ?: ""
        } else "")
        val font = remix.main.defaultFont
        val color = getRenderColor(editor, editor.theme)
        val oldColor = batch.packedColor
        val oldFontSizeX = font.data.scaleX
        val oldFontSizeY = font.data.scaleY
        val selectionTint = editor.theme.entities.selectionTint
        val showSelection = isSelected

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        // filled rect + border
        batch.setColorWithTintIfNecessary(selectionTint, color, necessary = showSelection)
        batch.fillRect(x, y,
                       width, height)

        batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                                          (color.g - 0.25f).coerceIn(0f, 1f),
                                          (color.b - 0.25f).coerceIn(0f, 1f),
                                          color.a, necessary = showSelection)

        if (this is IStretchable && this.isStretchable) {
            val oldColor = batch.packedColor
            val arrowWidth: Float = Math.min(width / 2f, Editor.ENTITY_HEIGHT / Editor.ENTITY_WIDTH)
            val y = y + height / 2 - 0.5f
            val arrowTex = AssetRegistry.get<Texture>("entity_stretchable_arrow")

            batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                                              (color.g - 0.25f).coerceIn(0f, 1f),
                                              (color.b - 0.25f).coerceIn(0f, 1f),
                                              color.a * 0.5f, necessary = showSelection)

            batch.draw(arrowTex, x + arrowWidth, y, width - arrowWidth * 2, 1f,
                       arrowTex.width / 2, 0, arrowTex.width / 2,
                       arrowTex.height, false, false)
            batch.draw(arrowTex, x, y, arrowWidth, 1f,
                       0, 0, arrowTex.width / 2, arrowTex.height, false, false)
            batch.draw(arrowTex, x + width, y, -arrowWidth, 1f,
                       0, 0, arrowTex.width / 2, arrowTex.height, false, false)

            batch.packedColor = oldColor
        }

        batch.drawRect(x, y,
                       width, height,
                       editor.toScaleX(BORDER), editor.toScaleY(BORDER))

        renderBeforeText(editor, batch)

        batch.setColor(1f, 1f, 1f, 0.5f)
        val iconSizeY = 1f - 4 * (editor.toScaleY(BORDER))
        val iconSizeX = editor.toScaleX(iconSizeY * Editor.ENTITY_HEIGHT)

        batch.draw(game.icon,
                   x + 2 * (editor.toScaleX(BORDER)),
                   y + 2 * editor.toScaleY(BORDER) + ((height - 4 * editor.toScaleY(
                           BORDER)) - iconSizeY) / 2,
                   iconSizeX, iconSizeY)

        batch.packedColor = oldColor
        val oldFontColor = font.color
        val fontScale = 0.6f
        font.color = textColor
        font.data.setScale(oldFontSizeX * fontScale, oldFontSizeY * fontScale)
        // width - iconSizeX - 6 * (editor.toScaleX(BORDER))
        val allottedWidth = width - 2 * (editor.toScaleX(BORDER))
        val allottedHeight = height - 4 * (editor.toScaleY(BORDER))

        val textHeight = font.getTextHeight(text, allottedWidth, true)
        val textX = x + 1 * (editor.toScaleX(BORDER))
        val textY = y + height / 2
        if (textHeight > allottedHeight) {
            val ratio = Math.min(allottedWidth / (font.getTextWidth(text, allottedWidth, false)), allottedHeight / textHeight)
            font.data.setScale(ratio * font.data.scaleX, ratio * font.data.scaleY)
        }
        needsNameTooltip = textHeight > allottedHeight
        var newTextWidth = allottedWidth
        if (attemptTextOnScreen) {
            val camera = editor.camera
            val outerBound = camera.position.x + camera.viewportWidth / 2 * camera.zoom
            if (textX + newTextWidth > outerBound) {
                newTextWidth = (outerBound) - textX
            }
            newTextWidth = newTextWidth.coerceAtLeast(font.getTextWidth(text)).coerceAtMost(allottedWidth)
        }
        font.draw(batch, text, textX, textY + font.getTextHeight(text, newTextWidth, true) / 2, newTextWidth, Align.right, true)

        when (editor.scrollMode) {
            Editor.ScrollMode.PITCH -> {
                if (this is IRepitchable && (this.canBeRepitched || this.semitone != 0)) {
                    drawCornerText(editor, batch, getTextForSemitone(semitone), !this.canBeRepitched, x, y)
                }
            }
            Editor.ScrollMode.VOLUME -> {
                if (this is IVolumetric && (this.isVolumetric || this.volumePercent != IVolumetric.DEFAULT_VOLUME)) {
                    drawCornerText(editor, batch, IVolumetric.getVolumeText(this.volumePercent), !this.isVolumetric, x, y)
                }
            }
        }
        font.color = oldFontColor
        font.data.setScale(oldFontSizeX, oldFontSizeY)
    }

    private fun drawCornerText(editor: Editor, batch: SpriteBatch, text: String, useNegativeColor: Boolean, x: Float, y: Float) {
        val borderedFont = remix.main.defaultBorderedFont
        editor.apply {
            borderedFont.scaleFont(editor.camera)
        }
        borderedFont.scaleMul(0.75f)
        if (useNegativeColor) {
            borderedFont.setColor(1f, 0.8f, 0.8f, 1f)
        } else {
            borderedFont.setColor(1f, 1f, 1f, 1f)
        }
        borderedFont.draw(batch, text,
                          x + 2 * editor.toScaleX(BORDER),
                          y + 2 * editor.toScaleY(BORDER) + borderedFont.capHeight)
        editor.apply {
            borderedFont.unscaleFont()
        }
    }
}