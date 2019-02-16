package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import kotlin.math.roundToInt


fun Editor.renderPlayalong(batch: SpriteBatch, beatRange: IntRange) {
    val largeFont = this.main.defaultBorderedFontLarge
    largeFont.scaleFont(camera)
    val playalong = remix.playalong

    val baseY = camera.position.y + 1.5f

    for (inputAction in playalong.inputActions) {
        if (inputAction.beat.roundToInt() !in beatRange && (inputAction.beat + inputAction.duration).roundToInt() !in beatRange) continue
        largeFont.setColor(1f, 1f, 1f, 1f)

        // Backing line
        if (!inputAction.isInstantaneous) {
            batch.setColor(0f, 0f, 0f, 1f)
            batch.fillRect(inputAction.beat, baseY - 0.5f, inputAction.duration, 1f)
        }

        val results = playalong.inputted[inputAction]
        val inProgress = playalong.inputsInProgress[inputAction]
        if (inProgress != null) largeFont.setColor(0f, 0f, 1f, 1f)
        if (results != null && results.results.isNotEmpty()) {
            if (!results.missed) {
                largeFont.setColor(0f, 1f, 0f, 1f)
            } else {
                largeFont.setColor(1f, 0f, 0f, 1f)
            }

            if (results.missed) {
                batch.setColor(1f, 0f, 0f, 1f)
            } else {
                batch.setColor(0f, 1f, 0f, 1f)
            }
        }

        // For non-instantaneous inputs, draw a long line (progress)
        if (!inputAction.isInstantaneous) {
            val defWidth = inputAction.duration
            val width = if (inProgress != null) {
                batch.setColor(0f, 0f, 1f, 1f)
                (remix.beat - inputAction.beat)
            } else if (results != null) {
                (defWidth + (remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds(inputAction.beat + inputAction.duration) + results.results.last().offset) - (inputAction.beat + inputAction.duration)))
            } else 0f
            batch.fillRect(inputAction.beat, baseY - 0.5f, width, 1f)
        }

        val estHeight = largeFont.getTextHeight(inputAction.input.tallDisplayText)
        val scale = if (estHeight > 2.5f) {
            2.5f / estHeight
        } else 1f
        largeFont.scaleMul(scale)
        val width = largeFont.getTextWidth(inputAction.input.tallDisplayText)
        val height = largeFont.getTextHeight(inputAction.input.tallDisplayText)
        val x = inputAction.beat
        val y = baseY
        val boxWidth = width * 1.1f
        val boxHeight = height * 1.5f
        batch.setColor(0f, 0f, 0f, 0.4f)
        batch.fillRect(x - boxWidth / 2, y - boxHeight / 2, boxWidth, boxHeight)
        // width * 0.02 is for correcting a glyph error in the font
        largeFont.draw(batch, inputAction.input.tallDisplayText, x + width * 0.02f, y + height / 2, 0f, Align.center, false)
        largeFont.setColor(1f, 1f, 1f, 1f)
        largeFont.scaleMul(1f / scale)
    }

    if (playalong.skillStarEntity != null) {
        largeFont.color = Color.YELLOW
        val star = "★"
        largeFont.draw(batch, star, playalong.skillStarEntity.bounds.x, baseY + largeFont.capHeight - 2.5f, 0f, Align.center, false)
        largeFont.setColor(1f, 1f, 1f, 1f)
    }

    batch.setColor(1f, 1f, 1f, 1f)
    largeFont.unscaleFont()
}
