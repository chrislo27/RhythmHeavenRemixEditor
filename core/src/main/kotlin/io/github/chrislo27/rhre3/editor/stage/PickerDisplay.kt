package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
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
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
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
                    labels.forEachIndexed { index, label ->
                        val half = number / 2
                        if ((scrollValue - index).absoluteValue > half + 1){
                            return@forEachIndexed
                        }

                        val selected = index == selection.currentIndex
                        font.color = if (selected && editor.currentTool == Tool.SELECTION) Editor.SELECTED_TINT else label.color
                        font.drawCompressed(batch, label.string,
                                            location.realX,
                                            location.realY + location.realHeight / 2 + font.capHeight / 2
                                                    + sectionY * (scrollValue - index),
                                            location.realWidth, Align.left)
                    }

                    font.setColor(1f, 1f, 1f, 1f)
                }
    }

    data class Label(var string: String, var color: Color)

}