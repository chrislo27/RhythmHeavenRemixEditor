package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.timesignature.TimeSignature
import io.github.chrislo27.rhre3.util.RectanglePool
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


fun Editor.renderPlayYan(batch: SpriteBatch) {
    val beat = if (remix.playState != PlayState.STOPPED) remix.beat else remix.playbackStart
    if (remix.playState != PlayState.STOPPED) {
        val timeSig = remix.timeSignatures.getTimeSignature(beat)
        val interval = timeSig?.noteFraction ?: 1f
        val beatPercent = (beat - (timeSig?.beat ?: 0f)) % interval
        val playbackStartPercent = (remix.playbackStart - (timeSig?.beat ?: 0f)) % interval
        val floorPbStart = Math.floor(playbackStartPercent.toDouble()).toFloat()
        val jumpHeight: Float = MathUtils.sin(MathUtils.PI / interval * (if (playbackStartPercent > 0f && remix.beat < floorPbStart + 1f) (beat - remix.playbackStart) / (1f - playbackStartPercent) else beatPercent)).absoluteValue

        val currentSwing = remix.tempos.swingAt(beat)
        batch.draw(AssetRegistry.get<Texture>(if (currentSwing.ratio == 50) "playyan_jumping" else "playyan_pogo"), beat,
                   remix.trackCount + 1f * jumpHeight, toScaleX(26f), toScaleY(35f),
                   0, 0, 26, 35, false, false)
    } else {
        val step = (MathHelper.getSawtoothWave(0.25f) * 4).toInt()
        batch.draw(AssetRegistry.get<Texture>("playyan_walking"), beat,
                   remix.trackCount * 1f,
                   toScaleX(26f), toScaleY(35f),
                   step * 26, 0, 26, 35, false, false)
    }
}

private fun Editor.renderTimeSignature(batch: SpriteBatch, beat: Float, lowerText: String, upperText: String, bigFont: BitmapFont, heightOfTrack: Float) {
    val x = beat
    val startY = 0f + toScaleY(Editor.TRACK_LINE_THICKNESS)
    val maxWidth = 1f

    val lowerWidth = bigFont.getTextWidth(lowerText, maxWidth, false).coerceAtMost(maxWidth)
    val upperWidth = bigFont.getTextWidth(upperText, maxWidth, false).coerceAtMost(maxWidth)
    val biggerWidth = Math.max(lowerWidth, upperWidth)

    bigFont.drawCompressed(batch, lowerText,
                           x + biggerWidth * 0.5f - lowerWidth * 0.5f,
                           startY + bigFont.capHeight,
                           maxWidth, Align.left)
    bigFont.drawCompressed(batch, upperText,
                           x + biggerWidth * 0.5f - upperWidth * 0.5f,
                           startY + heightOfTrack,
                           maxWidth, Align.left)
}

fun Editor.renderTimeSignatures(batch: SpriteBatch, beatRange: IntRange) {
    val timeSignatures = remix.timeSignatures
    val bigFont = main.timeSignatureFont
    val heightOfTrack = remix.trackCount.toFloat() - toScaleY(Editor.TRACK_LINE_THICKNESS) * 2f
    val inputX = camera.getInputX()
    val inputBeat = MathHelper.snapToNearest(inputX, snap)
    bigFont.scaleFont(camera)
    bigFont.scaleMul((heightOfTrack * 0.5f - 0.075f * (heightOfTrack / Editor.DEFAULT_TRACK_COUNT)) / bigFont.capHeight)

    timeSignatures.map.values.forEach { timeSig ->
        if (timeSig.beat.roundToInt() !in beatRange) return@forEach
        if (currentTool == Tool.TIME_SIGNATURE && MathUtils.isEqual(timeSig.beat, inputBeat) && remix.playState == PlayState.STOPPED) {
            bigFont.color = theme.selection.selectionBorder
        } else {
            bigFont.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.75f)
        }

        renderTimeSignature(batch, timeSig.beat, timeSig.lowerText, timeSig.upperText, bigFont, heightOfTrack)
    }

    if (currentTool == Tool.TIME_SIGNATURE && remix.timeSignatures.map[inputBeat] == null && remix.playState == PlayState.STOPPED) {
        bigFont.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * MathUtils.lerp(0.2f, 0.35f, MathHelper.getTriangleWave(2f)))
        val last = remix.timeSignatures.getTimeSignature(inputBeat)
        renderTimeSignature(batch, inputBeat, last?.lowerText ?: TimeSignature.DEFAULT_NOTE_UNIT.toString(), last?.upperText ?: TimeSignature.DEFAULT_NOTE_UNIT.toString(), bigFont, heightOfTrack)
    }

    bigFont.setColor(1f, 1f, 1f, 1f)
    bigFont.unscaleFont()
}

fun Editor.renderBeatNumbers(batch: SpriteBatch, beatRange: IntRange, font: BitmapFont) {
    val width = Editor.ENTITY_WIDTH * 0.4f
    val y = remix.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS + Editor.TRACK_LINE_THICKNESS) + font.capHeight
    // Render quarter note beat numbers/lines
    for (i in beatRange) {
        val x = i - width / 2f
        val text = if (i == 0) Editor.ZERO_BEAT_SYMBOL else "${Math.abs(i)}"
        if (stage.jumpToField.hasFocus && i == stage.jumpToField.text.toIntOrNull() ?: Int.MAX_VALUE) {
            val glow = MathHelper.getTriangleWave(1f)
            val sel = theme.selection.selectionBorder
            font.setColor(MathUtils.lerp(sel.r, 1f, glow), MathUtils.lerp(sel.g, 1f, glow),
                          MathUtils.lerp(sel.b, 1f, glow), sel.a)
        } else {
            font.color = theme.trackLine
        }
        font.drawCompressed(batch, text,
                            x, y, width, Align.center)
        if (i < 0) {
            val textWidth = font.getTextWidth(text, width, false)
            font.drawCompressed(batch, Editor.NEGATIVE_SYMBOL, x - textWidth / 2f, y, Editor.ENTITY_WIDTH * 0.2f, Align.right)
        }
    }

    // Render measure based beat numbers
    val minInterval = 4f / TimeSignature.NOTE_UNITS.last()
    var i = MathHelper.snapToNearest(beatRange.first.toFloat(), minInterval)
    var lastMeasureRendered = -1
    while (i <= MathHelper.snapToNearest(beatRange.last.toFloat(), minInterval)) {
        val x = i - width / 2f
        val measureNum = remix.timeSignatures.getMeasure(i)
        if (measureNum >= 1 && measureNum != lastMeasureRendered && remix.timeSignatures.getMeasurePart(i) == 0 && i < remix.duration) {
            lastMeasureRendered = measureNum
            font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
            font.drawCompressed(batch, "$measureNum",
                                x, y + font.lineHeight, width, Align.center)
        }
        i += minInterval
    }

    font.setColor(1f, 1f, 1f, 1f)
}

fun Editor.renderBeatLines(batch: SpriteBatch, beatRange: IntRange, trackYOffset: Float, updateDelta: Boolean) {
    for (i in beatRange) {
        batch.color = theme.trackLine
        if (remix.timeSignatures.getMeasurePart(i.toFloat()) > 0) {
            batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.25f)
        }

        val xOffset = toScaleX(Editor.TRACK_LINE_THICKNESS) / -2
        batch.fillRect(i.toFloat() + xOffset, trackYOffset, toScaleX(Editor.TRACK_LINE_THICKNESS),
                       remix.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))

        val flashAnimation = subbeatSection.flashAnimation > 0
        val actuallyInRange = (subbeatSection.enabled && i.toFloat() in subbeatSection.start..subbeatSection.end)
        if (flashAnimation || actuallyInRange) {
            batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b,
                           theme.trackLine.a * 0.3f *
                                   if (!actuallyInRange) subbeatSection.flashAnimation else 1f)
            for (j in 1 until Math.round(1f / snap)) {
                batch.fillRect(i.toFloat() + snap * j + xOffset, trackYOffset, toScaleX(Editor.TRACK_LINE_THICKNESS),
                               remix.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))
            }
        }
    }

    // Render measure based beat numbers
    val minInterval = 4f / TimeSignature.NOTE_UNITS.last()
    var i = MathHelper.snapToNearest(beatRange.first.toFloat(), minInterval)
    var lastMeasurePtRendered = -1
    var lastMeasureRendered = -1
    while (i <= MathHelper.snapToNearest(beatRange.last.toFloat(), minInterval)) {
        val measurePart = remix.timeSignatures.getMeasurePart(i)
        val measure = remix.timeSignatures.getMeasure(i)
        if (lastMeasurePtRendered != measurePart || lastMeasureRendered != measure) {
            lastMeasurePtRendered = measurePart
            lastMeasureRendered = measure
            if (measurePart > 0) {
                batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.2f)
            } else {
                batch.color = theme.trackLine
            }

            val thickness = Editor.TRACK_LINE_THICKNESS * (if (measurePart == 0) 3 else 1)
            val xOffset = toScaleX(thickness) / -2
            batch.fillRect(i + xOffset, trackYOffset, toScaleX(thickness),
                           remix.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))
        }
        i += minInterval
    }

    batch.setColor(1f, 1f, 1f, 1f)

    if (subbeatSection.flashAnimation > 0 && updateDelta) {
        subbeatSection.flashAnimation -= Gdx.graphics.deltaTime / subbeatSection.flashAnimationSpeed
        if (subbeatSection.flashAnimation < 0)
            subbeatSection.flashAnimation = 0f
    }
}

fun Editor.renderHorizontalTrackLines(batch: SpriteBatch, startX: Float, width: Float, trackYOffset: Float) {
    batch.color = theme.trackLine
    for (i in 0..remix.trackCount) {
        batch.fillRect(startX, trackYOffset + i.toFloat(), width,
                       toScaleY(Editor.TRACK_LINE_THICKNESS))
    }
    batch.setColor(1f, 1f, 1f, 1f)
}

fun Editor.renderStripeBoard(batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
    val clickOccupation = clickOccupation
    if (clickOccupation is ClickOccupation.SelectionDrag) {
        val oldColor = batch.packedColor
        val rect = RectanglePool.obtain()
        rect.set(clickOccupation.lerpLeft, clickOccupation.lerpBottom, clickOccupation.lerpRight - clickOccupation.lerpLeft, clickOccupation.lerpTop - clickOccupation.lerpBottom)

        val overStoreArea = pickerSelection.filter == stage.storedPatternsFilter && stage.pickerStage.isMouseOver() && !stage.patternAreaStage.isMouseOver()
        if ((!clickOccupation.isPlacementValid() || clickOccupation.isInDeleteZone()) && !overStoreArea) {
            batch.setColor(1f, 0f, 0f, 0.15f)
            batch.fillRect(rect)

            shapeRenderer.projectionMatrix = camera.combined
            shapeRenderer.prepareStencilMask(batch) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
                shapeRenderer.end()
            }.useStencilMask {
                val tex = AssetRegistry.get<Texture>("ui_stripe_board")
                val scale = 2f
                val w = tex.width.toFloat() / RHRE3.WIDTH * camera.viewportWidth / scale
                val h = tex.height.toFloat() / RHRE3.HEIGHT * camera.viewportHeight / scale
                for (x in 0..(RHRE3.WIDTH / tex.width * scale).roundToInt() + 2) {
                    for (y in 0..(RHRE3.HEIGHT / tex.height * scale).roundToInt() + 2) {
                        batch.draw(tex, x * w - camera.viewportWidth / 2 * camera.zoom + camera.position.x, y * h - camera.viewportHeight / 2 * camera.zoom + camera.position.y, w, h)
                    }
                }
            }
            batch.setColor(1f, 0f, 0f, 0.5f)
            batch.drawRect(rect, toScaleX(Editor.SELECTION_BORDER) * 2, toScaleY(Editor.SELECTION_BORDER) * 2)
        }

        batch.packedColor = oldColor
        RectanglePool.free(rect)
    }
}

fun Editor.renderImplicitTempo(batch: SpriteBatch) {
    val text = "The tempo is implicitly set to ${remix.tempos.defaultTempo} BPM. Please set the tempo explicitly using the Tempo Change tool.   "
    val f = main.defaultBorderedFontLarge
    f.setColor(1f, 1f, 1f, 1f)
    f.scaleMul(0.5f)

    for (i in 0 until 12) {
        val width = f.getTextWidth(text)
        val sign = if (i % 2 == 0) -1 else 1
        val scroll = MathHelper.getSawtoothWave(System.currentTimeMillis() + i * 1234L, 12.5f + sign * i * 0.75f)
        f.color = Color().setHSB(scroll, 1f, 1f)
        f.draw(batch, text, 0f + sign * width * scroll, i * main.defaultCamera.viewportHeight / 8f)
        f.draw(batch, text, -sign * width + sign * width * scroll, i * main.defaultCamera.viewportHeight / 8f)
    }
    f.setColor(1f, 1f, 1f, 1f)
    f.scaleMul(1 / 0.5f)
}

fun Editor.renderMining(batch: SpriteBatch, entity: ModelEntity<*>) {
    val mining = miningProgress
    if (mining != null && mining.entity == entity) {
        val breaking = AssetRegistry.get<Texture>("ui_breaking")
        val portion = MathUtils.lerp(0f, 10f, mining.progress).toInt().coerceIn(0, 9)
        val scale = 1f
        val height = scale
        val width = height / 4f
        for (x in 0..(entity.bounds.width / width).roundToInt()) {
            for (y in 0..(entity.bounds.height / height).roundToInt()) {
                val renderX = entity.bounds.x + width * x
                val renderY = entity.bounds.y + height * y
                val cutX = ((Math.min(entity.bounds.maxX, renderX + width) - renderX) / width).coerceIn(0f, 1f)
                val cutY = ((Math.min(entity.bounds.maxY, renderY + height) - renderY) / height).coerceIn(0f, 1f)
                batch.draw(breaking, renderX, renderY, width * cutX, height * cutY, portion / 10f, 1f, (portion + cutX) / 10f, 0f)
            }
        }
    }
}

fun Editor.renderParticles(batch: SpriteBatch) {
    val rect = RectanglePool.obtain().also {
        it.set(camera.position.x - camera.viewportWidth * camera.zoom / 2f, camera.position.y - camera.viewportHeight * camera.zoom / 2f, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom)
    }
    val pRect = RectanglePool.obtain()
    particles.forEach { particle ->
        pRect.set(particle.x - particle.width / 2, particle.y - particle.height / 2, particle.width, particle.height)
        if (pRect.intersects(rect)) {
            batch.color = particle.color
            batch.fillRect(pRect)
        }
        val delta = Gdx.graphics.deltaTime
        particle.x += particle.veloX * delta
        particle.y += particle.veloY * delta
        particle.veloX += particle.accelX * delta
        particle.veloY += particle.accelY * delta
        particle.expiry -= delta
    }
    particles.removeIf { it.expiry <= 0f }
    batch.setColor(1f, 1f, 1f, 1f)
    RectanglePool.free(rect)
    RectanglePool.free(pRect)
}
