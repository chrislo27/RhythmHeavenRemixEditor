package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask
import kotlin.math.absoluteValue


class PickerDisplay(val editor: Editor, val number: Int, val palette: UIPalette, parent: UIElement<EditorScreen>,
                    stage: Stage<EditorScreen>)
    : UIElement<EditorScreen>(parent, stage) {

    val labels: MutableList<Label> = mutableListOf()

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val filter = editor.pickerSelection.filter
        if (filter.areDatamodelsEmpty)
            return
        val selection = filter.currentDatamodelList ?: return
        val oldScroll = selection.smoothScroll
        selection.smoothScroll = MathUtils.lerp(oldScroll, selection.currentIndex.toFloat(),
                                                Gdx.graphics.deltaTime / 0.075f)
        val scrollValue = selection.smoothScroll

        shapeRenderer.prepareStencilMask(batch) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.rect(location.realX, location.realY, location.realWidth, location.realHeight)
            shapeRenderer.end()
        }.useStencilMask {
            val font = editor.main.defaultBorderedFont
            val sectionY = location.realHeight / number
            val currentTool = editor.currentTool
            labels.forEachIndexed { index, label ->
                val half = number / 2
                if ((scrollValue - index).absoluteValue > half + 1) {
                    return@forEachIndexed
                }

                val selected = index == selection.currentIndex
                font.color = if (selected && currentTool == Tool.SELECTION) Editor.SELECTED_TINT else label.color
                val mainWidth = font.getTextWidth(label.main)
                val availableWidth = location.realWidth
                val drawX = location.realX
                val drawY = location.realY + location.realHeight / 2 + sectionY * (scrollValue - index)
                val shouldUseNewAlpha = currentTool != Tool.SELECTION
                val newAlpha = 0.4f
                if (label.sub.isEmpty()) {
                    drawTextWithAlpha(batch, font, label.main, drawX, drawY + font.capHeight / 2, availableWidth, Align.left, false, newAlpha, shouldUseNewAlpha)
                } else {
                    val subScale = 0.75f
                    font.scaleMul(subScale)
                    val subWidth = font.getTextWidth(label.sub)
                    val totalWidth = mainWidth + subWidth + font.getTextWidth(" ")
                    font.scaleMul(1f / subScale)
                    val oldScaleX = font.scaleX
                    val oldScaleY = font.scaleY
                    val scaleFactor = if (totalWidth > availableWidth) availableWidth / totalWidth else 1f
                    val oldCapHeight = font.capHeight
                    font.data.scaleX = scaleFactor * oldScaleX
                    drawTextWithAlpha(batch, font, label.main, drawX, drawY + oldCapHeight / 2, availableWidth, Align.left, false, newAlpha, shouldUseNewAlpha)
                    font.scaleMul(subScale)
                    drawTextWithAlpha(batch, font, " " + label.sub, drawX + mainWidth * scaleFactor, drawY - oldCapHeight / 2 + font.capHeight, availableWidth, Align.left, false, newAlpha, shouldUseNewAlpha)
                    font.data.setScale(oldScaleX, oldScaleY)
                }
            }

            font.setColor(1f, 1f, 1f, 1f)
        }
    }

    private fun drawTextWithAlpha(batch: SpriteBatch, font: BitmapFont, text: String, drawX: Float, drawY: Float, availableWidth: Float, align: Int, wrap: Boolean, alpha: Float, setAlpha: Boolean) {
        val textWidth = font.getTextWidth(text)
        val oldScaleX = font.data.scaleX
        if (textWidth > availableWidth) {
            font.data.scaleX = (availableWidth / textWidth) * oldScaleX
        }
        font.cache.also { cache ->
            cache.clear()
            cache.addText(text, drawX, drawY, availableWidth, align, wrap)
            if (setAlpha) {
                cache.setAlphas(alpha)
            }
            cache.draw(batch)
        }
        font.data.scaleX = oldScaleX
    }

    data class Label(var main: String, var sub: String, var color: Color)

}