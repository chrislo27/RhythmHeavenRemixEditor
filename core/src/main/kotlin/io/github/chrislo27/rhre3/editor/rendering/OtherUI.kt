package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.util.RulerUtils
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


fun Editor.renderOtherUI(batch: SpriteBatch, beatRange: IntRange, font: BitmapFont) {
    val clickOccupation = clickOccupation
    when (clickOccupation) {
        is ClickOccupation.SelectionDrag -> {
            val oldColor = batch.packedColor
            val y = if (clickOccupation.isBottomSpecial) -1f else 0f
            val mouseY = camera.getInputY()
            val alpha = (1f + y - mouseY).coerceIn(0.5f + MathHelper.getTriangleWave(2f) * 0.125f, 1f)
            val left = camera.position.x - camera.viewportWidth / 2 * camera.zoom
            val overStoreArea = pickerSelection.filter == stage.storedPatternsFilter && stage.pickerStage.isMouseOver() && !stage.patternAreaStage.isMouseOver()

            if (!overStoreArea) {
                batch.setColor(1f, 0f, 0f, 0.25f * alpha)
                batch.fillRect(left, y,
                               camera.viewportWidth * camera.zoom,
                               -camera.viewportHeight * camera.zoom)
                batch.packedColor = oldColor

                val deleteFont = main.defaultFontLarge
                deleteFont.scaleFont(camera)
                deleteFont.scaleMul(0.5f)

                deleteFont.setColor(0.75f, 0.5f, 0.5f, alpha)

                deleteFont.drawCompressed(batch, Localization["editor.delete"], left, y + -1f + font.capHeight / 2,
                                          camera.viewportWidth * camera.zoom, Align.center)

                deleteFont.setColor(1f, 1f, 1f, 1f)
                deleteFont.unscaleFont()
            }
        }
        is ClickOccupation.CreatingSelection -> {
            val oldColor = batch.packedColor
            val rect = clickOccupation.rectangle
            val selectionFill = theme.selection.selectionFill
            val selectionMode = getSelectionMode()

            batch.setColor(selectionFill.r, selectionFill.g, selectionFill.b, selectionFill.a * 0.85f)
            remix.entities.forEach {
                if (selectionMode.wouldEntityBeIncluded(it, rect, remix.entities, this.selection)) {
                    batch.fillRect(it.bounds)
                }
            }

            batch.color = selectionFill
            batch.fillRect(rect)

            batch.color = theme.selection.selectionBorder
            batch.drawRect(rect, toScaleX(Editor.SELECTION_BORDER), toScaleY(Editor.SELECTION_BORDER))

            run text@{
                val oldFontColor = font.color
                font.color = theme.selection.selectionBorder

                val toScaleX = toScaleX(Editor.SELECTION_BORDER * 1.5f)
                val toScaleY = toScaleY(Editor.SELECTION_BORDER * 1.5f)
                val shift = Gdx.input.isShiftDown()
                val control = Gdx.input.isControlDown()

                val bigFont = main.defaultFontLarge
                val oldBigFontColor = bigFont.color
                bigFont.scaleFont(camera)

                // AND or XOR strings
                if (rect.height - toScaleY * 2 >= bigFont.capHeight
                        && !(shift && control) && (shift || control)) {
                    bigFont.color = theme.selection.selectionBorder
                    bigFont.color.a *= 0.25f * MathHelper.getTriangleWave(2f) + 0.35f
                    bigFont.drawCompressed(batch, if (shift) Editor.SELECTION_RECT_ADD else Editor.SELECTION_RECT_INVERT,
                                           rect.x + toScaleX, rect.y + rect.height / 2 + bigFont.capHeight / 2,
                                           rect.width - toScaleX * 2, Align.center)
                }

                // dimension strings
                if (rect.height - toScaleY * 2 >= font.capHeight) {
                    font.color = theme.trackLine

                    val moddingEnabled = ModdingUtils.moddingToolsEnabled
                    var widthStr = Editor.TWO_DECIMAL_PLACES_FORMATTER.format(rect.width.toDouble())

                    if (moddingEnabled) {
                        // X * 0x30 [+ 0xY]
                        widthStr += "\n${ModdingUtils.currentGame.beatsToTickflowString(rect.width)}"
                    }

                    var defaultX = rect.x + toScaleX
                    var defaultWidth = rect.width - toScaleX * 2
                    if (defaultX < camera.position.x - camera.viewportWidth / 2 * camera.zoom) {
                        defaultX = camera.position.x - camera.viewportWidth / 2 * camera.zoom
                        defaultWidth = (rect.width + rect.x) - defaultX - toScaleX
                    } else if (defaultX + defaultWidth > camera.position.x + camera.viewportWidth / 2) {
                        defaultWidth = (camera.position.x + camera.viewportWidth / 2) - defaultX
                    }
                    if (rect.width - toScaleX * 2 >= font.getTextWidth(widthStr) && rect.height - toScaleY * 2 >= font.getTextHeight(widthStr)) {
                        font.drawCompressed(batch, widthStr,
                                            defaultX,
                                            rect.y + rect.height - toScaleY,
                                            defaultWidth, Align.center)
                    }
                }

                bigFont.unscaleFont()
                bigFont.color = oldBigFontColor
                font.color = oldFontColor
            }

            batch.packedColor = oldColor
        }
        is ClickOccupation.RulerMeasuring -> {
            val oldColor = batch.packedColor
            val oldFontColor = font.color
            val currentSnap = if (Gdx.input.isShiftDown() && !Gdx.input.isControlDown() && !Gdx.input.isAltDown()) 0f else snap
            val mouseX = MathHelper.snapToNearest(camera.getInputX(), currentSnap)
            val width = (clickOccupation.startPoint.x - mouseX).absoluteValue
            val leftPoint = Math.min(clickOccupation.startPoint.x, mouseX)
            val rightPoint = Math.max(clickOccupation.startPoint.x, mouseX)
            val height = 1.5f
            val borderThickness = Editor.TRACK_LINE_THICKNESS * 1.5f
            val markThickness = Editor.TRACK_LINE_THICKNESS
            val y = 0f

            batch.color = theme.background
            batch.fillRect(leftPoint, y, width, -height)
            batch.color = theme.trackLine
            batch.drawRect(leftPoint - toScaleX(Editor.TRACK_LINE_THICKNESS) / 2, y - height, width + toScaleX(Editor.TRACK_LINE_THICKNESS), height, toScaleX(borderThickness), toScaleY(borderThickness))

            if (width > 0) {
                val leftTextPoint = Math.max(leftPoint + toScaleX(borderThickness), camera.position.x - camera.viewportWidth / 2)
                val rightTextPoint = Math.min(rightPoint - toScaleY(borderThickness), camera.position.x + camera.viewportWidth / 2)
                val textWidth = rightTextPoint - leftTextPoint
                font.color = theme.trackLine
                font.scaleMul(0.75f)
                // Mixed number notation, add decimal if no snapping
                font.drawCompressed(batch, RulerUtils.widthToMixedNumber(width, snap) + " ♩" + if (currentSnap == 0f) " (${Editor.TWO_DECIMAL_PLACES_FORMATTER.format(width)})" else "", leftTextPoint, y - height + toScaleY(borderThickness) + font.capHeight * 1.1f, textWidth, Align.center)
                if (ModdingUtils.moddingToolsEnabled) {
                    // tickflow notation
                    font.drawCompressed(batch, ModdingUtils.currentGame.beatsToTickflowString(width), leftTextPoint, y - height + toScaleY(borderThickness) + font.capHeight * 2.4f, textWidth, Align.center)
                }
                font.scaleMul(1 / 0.75f)
            }

            val divisions = (1f / snap).coerceAtMost(64f).roundToInt()
            val reverseRange = mouseX < clickOccupation.startPoint.x
            for (i in (0 until ((rightPoint - leftPoint) * divisions).toInt())) {
                val x = if (!reverseRange) (leftPoint + i / divisions.toFloat()) else (rightPoint - i / divisions.toFloat())
                // culling for rendering
                if (x < beatRange.first)
                    continue
                if (x > beatRange.last)
                    break

                var markElongation = 0f
                if (i % (divisions / 2) == 0)
                    markElongation += 0.75f
                if (divisions >= 8 && i % (divisions / 4) == 0)
                    markElongation += 0.5f
                if (divisions >= 16 && i % (divisions / 8) == 0)
                    markElongation += 0.35f
                batch.fillRect(x - toScaleX(markThickness) / (if (reverseRange) -2 else 2), y, toScaleX(markThickness) * if (reverseRange) -1 else 1, -0.35f * (markElongation * 0.5f + 1))
            }

            batch.packedColor = oldColor
            font.color = oldFontColor
        }
        else -> {
        }
    }
}
