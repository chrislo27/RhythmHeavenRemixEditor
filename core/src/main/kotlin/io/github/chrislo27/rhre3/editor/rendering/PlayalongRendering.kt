package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.roundToInt


fun Editor.renderPlayalong(batch: SpriteBatch, beatRange: IntRange) {
    val largeFont = this.main.defaultBorderedFontLarge
    largeFont.scaleFont(camera)
    largeFont.setColor(1f, 1f, 1f, 1f) // TODO

    for (inputAction in remix.playalong.inputActions) {
        if (inputAction.beat.roundToInt() !in beatRange && (inputAction.beat + inputAction.duration).roundToInt() !in beatRange) continue

        // For non-instantaneous inputs, draw a long line
        if (!inputAction.isInstantaneous) {
            batch.setColor(0f, 0f, 0f, 1f)
            batch.fillRect(inputAction.beat + 0.25f, 0.5f, inputAction.duration - 0.25f, 1f)
        }

        largeFont.draw(batch, inputAction.input.displayText, inputAction.beat, largeFont.capHeight)
    }

    batch.setColor(1f, 1f, 1f, 1f)
    largeFont.unscaleFont()
}
