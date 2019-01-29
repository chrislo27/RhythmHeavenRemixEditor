package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


fun Editor.renderGameBoundaryDividers(batch: SpriteBatch, beatRange: IntRange, font: BitmapFont) {
    remix.gameSections.values.forEach { section ->
        if (section.startBeat > beatRange.last || section.endBeat < beatRange.first)
            return@forEach
        val icon = section.game.icon
        val sectionWidth = section.endBeat - section.startBeat

        // dividing lines
        val triangle = AssetRegistry.get<Texture>("tracker_right_tri")
        run left@{
            batch.color = theme.trackLine
            font.color = theme.trackLine
            val x = section.startBeat
            val height = Editor.MIN_TRACK_COUNT * 2f + 0.5f + (remix.trackCount - Editor.MIN_TRACK_COUNT)
            val maxTextWidth = 5f
            batch.fillRect(x, 0f, toScaleX(Editor.TRACK_LINE_THICKNESS) * 2, height)
            batch.draw(triangle, x, height - 1f, 0.25f, 1f)

            for (i in 0 until (sectionWidth / 6f).toInt().coerceAtLeast(1)) {
                batch.setColor(1f, 1f, 1f, 1f)

                val left = x + (6f * i).coerceAtLeast(0.125f)

                batch.draw(icon, left, height - 2f, 0.25f, 1f)
                font.drawCompressed(batch,
                                    if (stage.presentationModeStage.visible) section.game.group else section.game.name,
                                    left, height - 2.25f,
                                    (sectionWidth - 0.25f).coerceIn(0f, maxTextWidth), Align.left)
            }
        }
        batch.setColor(1f, 1f, 1f, 1f)
        font.setColor(1f, 1f, 1f, 1f)
    }

    batch.setColor(1f, 1f, 1f, 1f)
}

fun Editor.renderGameBoundaryBg(batch: SpriteBatch, beatRange: IntRange) {
    val squareHeight = remix.trackCount.toFloat()
    val squareWidth = squareHeight / (Editor.ENTITY_WIDTH / Editor.ENTITY_HEIGHT)

    remix.gameSections.values.forEach { section ->
        if (section.startBeat > beatRange.last || section.endBeat < beatRange.first)
            return@forEach
        val tex = section.game.icon

        val sectionWidth = section.endBeat - section.startBeat
        val sections = (sectionWidth / squareWidth)
        val wholes = sections.toInt()
        val remainder = sectionWidth % squareWidth

        // track background icons
        batch.setColor(1f, 1f, 1f, 0.25f)
        for (i in 0 until wholes) {
            batch.draw(tex, section.startBeat + squareWidth * i, 0f,
                       squareWidth, squareHeight)
        }
        batch.draw(tex, section.startBeat + squareWidth * wholes, 0f,
                   remainder, squareHeight,
                   0, 0, (tex.width * (sections - wholes)).toInt(), tex.height,
                   false, false)
    }

    batch.setColor(1f, 1f, 1f, 1f)
}
