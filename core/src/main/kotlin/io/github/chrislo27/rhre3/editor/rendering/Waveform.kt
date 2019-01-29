package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.getValues
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


fun Editor.renderWaveform(batch: SpriteBatch, oldCameraX: Float, oldCameraY: Float, adjustedCameraX: Float, adjustedCameraY: Float) {
    batch.setColor(theme.waveform.r, theme.waveform.g, theme.waveform.b, theme.waveform.a * 0.65f)

    val samplesPerSecond = BeadsSoundSystem.audioContext.sampleRate.toInt()
    val samplesPerFrame = samplesPerSecond / 60
    val fineness: Int = (samplesPerFrame / 1).coerceAtMost(samplesPerFrame)

    val isPresentationMode = stage.presentationModeStage.visible
    val viewportWidth = if (isPresentationMode) camera.viewportWidth * 0.5f else camera.viewportWidth
    val height = if (!isPresentationMode) remix.trackCount / 2f else 1f
    val centre = remix.trackCount / 2f

    BeadsSoundSystem.audioContext.getValues(BeadsSoundSystem.sampleArray)

    val data = BeadsSoundSystem.sampleArray
    for (x in 0 until fineness) {
        val dataPoint = data[(x.toFloat() / fineness * data.size).toInt()]
        val h = height * (if (isPresentationMode) Math.abs(dataPoint) else dataPoint.coerceIn(-1f, 1f))
        val width = viewportWidth / fineness.toFloat() * camera.zoom
        if (!isPresentationMode) {
            batch.fillRect((camera.position.x - viewportWidth / 2 * camera.zoom) + x * width,
                           centre - h / 2, width, h)
        } else {
            val cameraAdjX = (oldCameraX - adjustedCameraX)
            val cameraAdjY = (oldCameraY - adjustedCameraY)
            batch.fillRect((camera.position.x - cameraAdjX - viewportWidth / 2 * camera.zoom) + x * width,
                           -3f - cameraAdjY, width, h / 2)
        }
    }

    batch.setColor(1f, 1f, 1f, 1f)
}