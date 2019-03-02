package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.playalong.PlayalongChars
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import kotlin.math.roundToInt


fun Editor.renderPlayalong(batch: SpriteBatch, beatRange: IntRange, alpha: Float) {
    if (alpha <= 0f) return
    val largeFont = this.main.defaultBorderedFontLarge
    largeFont.scaleFont(camera)
    val playalong = remix.playalong

    val recommendedHeight = largeFont.getTextHeight(PlayalongChars.FILLED_A)
    val recommendedWidth = largeFont.getTextWidth(PlayalongChars.FILLED_A)
    val blockHeight = recommendedHeight * 1.5f
    val blockWidth = recommendedWidth * 1.1f
    val baseY = camera.position.y + 1.5f
    val skillStarInputPair = playalong.skillStarInput
    val skillStarInput = skillStarInputPair?.first
    for ((beatAt, list) in playalong.inputActionsByBeat) {
        val listSize = list.size
        if (skillStarInput != null && skillStarInput.beat == beatAt) {
            val bottomY = baseY + (blockHeight * listSize) / 2 - (-1 + 0.5f) * blockHeight
            largeFont.setColor(1f, 1f, 0f, 1f * alpha)
            val star = "★"
            val width = largeFont.getTextWidth(star)
            val height = largeFont.getTextHeight(star)
            largeFont.draw(batch, star, skillStarInput.beat + (if (!skillStarInputPair.second) skillStarInput.duration else 0f) + width * 0.02f, bottomY + height / 2, 0f, Align.center, false)
            largeFont.setColor(1f, 1f, 1f, 1f)
        }
        list.forEachIndexed { index, inputAction ->
            if (inputAction.beat.roundToInt() !in beatRange && (inputAction.beat + inputAction.duration).roundToInt() !in beatRange) return@forEachIndexed
            largeFont.setColor(1f, 1f, 1f, 1f * alpha)

            val bottomY = baseY + (blockHeight * listSize) / 2 - (index + 0.5f) * blockHeight

            // Backing line
            if (!inputAction.isInstantaneous) {
                batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * alpha)
                batch.fillRect(inputAction.beat, bottomY - 0.5f, inputAction.duration, 1f)
            }

            val results = playalong.inputted[inputAction]
            val inProgress = playalong.inputsInProgress[inputAction]
            if (inProgress != null) largeFont.setColor(0.2f, 0.57f, 1f, 1f * alpha)
            if (results != null && results.results.isNotEmpty()) {
                if (!results.missed) {
                    largeFont.setColor(0.2f, 1f, 0.2f, 1f * alpha)
                    batch.setColor(0.2f, 1f, 0.2f, 1f * alpha)
                } else {
                    largeFont.setColor(1f, 0.15f, 0.15f, 1f * alpha)
                    batch.setColor(1f, 0.15f, 0.15f, 1f * alpha)
                }
            }

            // For non-instantaneous inputs, draw a long line (progress)
            if (!inputAction.isInstantaneous) {
                val defWidth = inputAction.duration
                val width = if (inProgress != null) {
                    batch.setColor(0.2f, 0.57f, 1f, 1f * alpha)
                    remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds((remix.beat - inputAction.beat)) - (if (inputAction.input.isTouchScreen) playalong.calibratedMouseOffset else playalong.calibratedKeyOffset))
                } else if (results != null) {
                    (defWidth + (remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds(inputAction.beat + inputAction.duration) + results.results.last().offset) - (inputAction.beat + inputAction.duration)))
                } else 0f
                batch.fillRect(inputAction.beat, bottomY - 0.5f, width.coerceAtLeast(0f), 1f)
            }

            val x = inputAction.beat
            val y = bottomY
            val boxWidth = blockWidth
            val boxHeight = blockHeight
            val lastBatchColor = batch.packedColor
            // Backing box
            batch.setColor(0f, 0f, 0f, 0.4f * alpha)
            batch.fillRect(x - boxWidth / 2, y - boxHeight / 2, boxWidth, boxHeight)
            batch.setColor(1f, 1f, 1f, 0.75f * alpha)
            val thinWidth = boxWidth * 0.05f
            batch.fillRect(x - thinWidth / 2, y - boxHeight / 2, thinWidth, boxHeight)
            batch.setColor(1f, 1f, 1f, 1f * alpha)

            // Render text or texture
            if (inputAction.input.trackDisplayIsTexID) {
                batch.packedColor = lastBatchColor
                batch.draw(AssetRegistry.get<Texture>(inputAction.input.trackDisplayText), x - boxWidth / 2, y - boxHeight / 2, boxWidth, boxHeight)
                batch.setColor(1f, 1f, 1f, 1f)
            } else {
                val estHeight = largeFont.getTextHeight(inputAction.input.trackDisplayText)
                val scaleY = if (estHeight > recommendedHeight) {
                    recommendedHeight / estHeight
                } else 1f
                largeFont.scaleMul(scaleY)
                val estWidth = largeFont.getTextWidth(inputAction.input.trackDisplayText)
                val scaleX = if (estWidth > recommendedWidth) {
                    recommendedWidth / estWidth
                } else 1f
                largeFont.scaleMul(scaleX)
                val width = largeFont.getTextWidth(inputAction.input.trackDisplayText)
                val height = largeFont.getTextHeight(inputAction.input.trackDisplayText)
                // width * 0.02 is for correcting a glyph error in the font
                largeFont.draw(batch, inputAction.input.trackDisplayText, x + width * 0.02f, y + height / 2, 0f, Align.center, false)
                largeFont.setColor(1f, 1f, 1f, 1f)
                largeFont.scaleMul(1f / scaleX)
                largeFont.scaleMul(1f / scaleY)
            }
        }
    }

    batch.setColor(1f, 1f, 1f, 1f)
    largeFont.unscaleFont()
}
