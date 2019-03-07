package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import kotlin.math.roundToInt


fun Editor.renderTopTrackers(batch: SpriteBatch, beatRange: IntRange, trackYOffset: Float) {
    // trackers (playback start, music)
    val borderedFont = main.defaultBorderedFont
    val oldFontColor = borderedFont.color

    fun getTrackerTime(time: Float, noBeat: Boolean = false): String {
        val signedSec = if (noBeat) time else remix.tempos.beatsToSeconds(time)
        val sec = Math.abs(signedSec)
        val seconds = (if (signedSec < 0) "-" else "") +
                Editor.TRACKER_MINUTES_FORMATTER.format(
                        (sec / 60).toLong()) + ":" + Editor.TRACKER_TIME_FORMATTER.format(
                sec % 60.0)
        if (noBeat) {
            return seconds
        }
        return Localization["tracker.any.time",
                Editor.THREE_DECIMAL_PLACES_FORMATTER.format(time.toDouble()), seconds]
    }

    fun renderAboveTracker(text: String?, controlText: String?, units: Int, beat: Float, color: Color,
                           trackerTime: String = getTrackerTime(beat),
                           triangleHeight: Float = 0.4f, bpmText: String? = null, showMusicUnsnap: Boolean = false) {
        val triangleWidth = toScaleX(triangleHeight * Editor.ENTITY_HEIGHT)
        val x = beat - toScaleX(Editor.TRACK_LINE_THICKNESS * 1.5f) / 2
        val y = trackYOffset
        val height = (remix.trackCount + 1.25f + 1.2f * units) + toScaleY(Editor.TRACK_LINE_THICKNESS)
        batch.packedColor = color.toFloatBits()
        batch.fillRect(x, y, toScaleX(Editor.TRACK_LINE_THICKNESS * 1.5f),
                       height - triangleHeight / 2)
        batch.draw(AssetRegistry.get<Texture>("tracker_right_tri"),
                   x, y + height - triangleHeight, triangleWidth, triangleHeight)

        borderedFont.scaleFont(camera)
        borderedFont.scaleMul(0.75f)
        borderedFont.color = batch.color
        if (text != null) {
            borderedFont.drawCompressed(batch, text, x - 1.05f, y + height, 1f, Align.right)
        }
        borderedFont.drawCompressed(batch, trackerTime, x + triangleWidth + 0.025f, y + height, 1f, Align.left)
        if (bpmText != null) {
            borderedFont.drawCompressed(batch, bpmText,
                                        x + triangleWidth + 0.025f,
                                        y + height + borderedFont.capHeight * 1.25f,
                                        1f, Align.left)
        }

        val line = borderedFont.lineHeight

        if (controlText != null) {
            borderedFont.scaleMul(0.75f)
            borderedFont.drawCompressed(batch, controlText, x - 1.05f, y + height - line, 1f,
                                        Align.right)
        }

        if (showMusicUnsnap) {
//                    borderedFont.scaleMul(0.75f)
            borderedFont.drawCompressed(batch, Localization["tracker.music.unsnap"], x + 0.05f, y + height - line, 1f,
                                        Align.left)
        }

        borderedFont.scaleFont(camera)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    if (cachedPlaybackStart.first != remix.tempos.beatsToSeconds(remix.playbackStart)) {
        cachedPlaybackStart = remix.tempos.beatsToSeconds(remix.playbackStart) to getTrackerTime(
                remix.playbackStart)
    }
    if (cachedMusicStart.first != remix.musicStartSec) {
        cachedMusicStart = remix.musicStartSec to getTrackerTime(remix.musicStartSec, noBeat = true)
    }

    renderAboveTracker(Localization["tracker.music"], Localization["tracker.music.controls"],
                       1, remix.musicStartSec, theme.trackers.musicStart,
                       cachedMusicStart.second, showMusicUnsnap = clickOccupation is ClickOccupation.Music)
    renderAboveTracker(Localization["tracker.playback"], Localization["tracker.playback.controls"],
                       0, remix.playbackStart, theme.trackers.playback, cachedPlaybackStart.second)

    if (stage.tapalongMarkersEnabled) {
        val tapalong = stage.tapalongStage
        tapalong.tapRecords.forEach {
            if (!it.remixSec.isNaN()) {
                val beat = remix.tempos.secondsToBeats(it.remixSec + remix.musicStartSec)
                if (beat.roundToInt() in beatRange) {
                    renderAboveTracker(null, null, 1, beat, theme.trackLine)
                }
            }
        }
    }

    if (remix.playState != PlayState.STOPPED) {
        val position = getApparentPlaybackTrackerPos()
        renderAboveTracker(null, null, 0, position,
                           theme.trackers.playback, triangleHeight = 0f,
                           bpmText = "♩=${Editor.ONE_DECIMAL_PLACE_FORMATTER.format(
                                   remix.tempos.tempoAt(remix.beat))}")
    }

    borderedFont.color = oldFontColor
    borderedFont.unscaleFont()
}
